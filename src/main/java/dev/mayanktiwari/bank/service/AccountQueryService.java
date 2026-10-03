package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.Customer;
import dev.mayanktiwari.bank.domain.EntryType;
import dev.mayanktiwari.bank.domain.LedgerEntry;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.repository.LedgerEntryRepository;
import dev.mayanktiwari.bank.support.CardNumbers;
import dev.mayanktiwari.bank.support.DataCipher;
import dev.mayanktiwari.bank.support.Formats;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only queries, returned as ready-to-print view objects so templates stay free of logic. */
@Service
public class AccountQueryService {

    public static final int STATEMENT_PAGE_SIZE = 20;

    public record AccountView(
            String holderName,
            String cardNumber,
            String accountType,
            String balance,
            String openedOn,
            String email,
            String address,
            String panMasked,
            String aadhaarMasked) {
    }

    public record EntryView(String when, String description, String withdrawn, String deposited, String balance) {
    }

    public record StatementView(
            List<EntryView> entries,
            int page,
            int totalPages,
            long totalEntries,
            boolean hasPrevious,
            boolean hasNext) {
    }

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final DataCipher cipher;
    private final AppProperties properties;

    public AccountQueryService(AccountRepository accounts, LedgerEntryRepository ledger,
                               DataCipher cipher, AppProperties properties) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.cipher = cipher;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public AccountView account(Long accountId) {
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account " + accountId + " does not exist"));
        Customer customer = account.getCustomer();
        ZoneId zone = properties.zone();

        String pan = cipher.decrypt(customer.getPanEncrypted());
        String panMasked = pan.length() >= 4 ? "XXXXXX" + pan.substring(pan.length() - 4) : "XXXXXXXXXX";

        return new AccountView(
                customer.getFullName(),
                CardNumbers.grouped(account.getCardNumber()),
                account.getAccountType().label(),
                Formats.rupees(account.getBalance()),
                Formats.date(account.getCreatedAt(), zone),
                customer.getEmail(),
                customer.getAddress() + ", " + customer.getCity() + " " + customer.getPinCode(),
                panMasked,
                "XXXX XXXX " + customer.getAadhaarLast4());
    }

    @Transactional(readOnly = true)
    public List<EntryView> recentEntries(Long accountId) {
        return ledger.findTop10ByAccountIdOrderByCreatedAtDescIdDesc(accountId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public StatementView statement(Long accountId, int requestedPage) {
        int page = Math.max(requestedPage, 0);
        Page<LedgerEntry> result = ledger.findByAccountIdOrderByCreatedAtDescIdDesc(
                accountId, PageRequest.of(page, STATEMENT_PAGE_SIZE));
        List<EntryView> entries = result.getContent().stream().map(this::toView).toList();
        return new StatementView(
                entries,
                page,
                result.getTotalPages(),
                result.getTotalElements(),
                page > 0,
                result.hasNext());
    }

    private EntryView toView(LedgerEntry entry) {
        boolean debit = entry.getEntryType() == EntryType.WITHDRAWAL
                || entry.getEntryType() == EntryType.TRANSFER_OUT;
        BigDecimal amount = entry.getAmount();
        return new EntryView(
                Formats.dateTime(entry.getCreatedAt(), properties.zone()),
                describe(entry),
                debit ? Formats.plainAmount(amount) : "",
                debit ? "" : Formats.plainAmount(amount),
                Formats.plainAmount(entry.getBalanceAfter()));
    }

    private static String describe(LedgerEntry entry) {
        String ending = CardNumbers.lastFour(entry.getCounterpartCard());
        return switch (entry.getEntryType()) {
            case DEPOSIT -> "Deposit";
            case WITHDRAWAL -> "Withdrawal";
            case TRANSFER_OUT -> "Transfer to account ending " + ending;
            case TRANSFER_IN -> "Transfer from account ending " + ending;
        };
    }
}
