package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.EntryType;
import dev.mayanktiwari.bank.domain.LedgerEntry;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Every method that moves money locks the account row first, so two requests for the same account
 * run one after the other. Transfers lock both rows in ascending id order so that two opposite
 * transfers cannot deadlock. Each request carries a request id, and the ledger has a unique
 * constraint on (account, request id), so a double click can never apply the same request twice.
 */
@Service
public class BankingService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final PinGuard pinGuard;
    private final AppProperties properties;
    private final Clock clock;

    public BankingService(AccountRepository accounts, LedgerEntryRepository ledger, PinGuard pinGuard,
                          AppProperties properties, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.pinGuard = pinGuard;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public OperationResult deposit(Long accountId, BigDecimal amount, UUID requestId) {
        BigDecimal value = normalise(amount);
        Account account = lock(accountId);
        Instant now = Instant.now(clock);

        if (ledger.existsByAccountIdAndRequestId(accountId, requestId)) {
            return result(Outcome.DUPLICATE, account);
        }
        if (overLimit(value)) {
            return result(Outcome.OVER_LIMIT, account);
        }
        account.credit(value);
        record(account, EntryType.DEPOSIT, value, null, requestId, now);
        return result(Outcome.OK, account);
    }

    @Transactional
    public OperationResult withdraw(Long accountId, BigDecimal amount, String pin, UUID requestId) {
        BigDecimal value = normalise(amount);
        Account account = lock(accountId);
        Instant now = Instant.now(clock);

        Outcome pinOutcome = pinGuard.check(account, pin, now);
        if (pinOutcome != null) {
            return result(pinOutcome, account);
        }
        if (ledger.existsByAccountIdAndRequestId(accountId, requestId)) {
            return result(Outcome.DUPLICATE, account);
        }
        if (overLimit(value)) {
            return result(Outcome.OVER_LIMIT, account);
        }
        if (!account.canCover(value)) {
            return result(Outcome.INSUFFICIENT_FUNDS, account);
        }
        account.debit(value);
        record(account, EntryType.WITHDRAWAL, value, null, requestId, now);
        return result(Outcome.OK, account);
    }

    @Transactional
    public OperationResult transfer(Long sourceId, String recipientCardNumber, BigDecimal amount,
                                    String pin, UUID requestId) {
        BigDecimal value = normalise(amount);
        Instant now = Instant.now(clock);

        // The recipient is looked up without a lock only to learn its id, so that both rows can be
        // locked in a fixed order. Nothing about the recipient is revealed before the PIN is checked.
        Long recipientId = accounts.findByCardNumber(recipientCardNumber)
                .map(Account::getId)
                .orElse(null);

        Account source;
        Account recipient = null;
        if (recipientId == null || recipientId.equals(sourceId)) {
            source = lock(sourceId);
        } else if (sourceId < recipientId) {
            source = lock(sourceId);
            recipient = lock(recipientId);
        } else {
            recipient = lock(recipientId);
            source = lock(sourceId);
        }

        Outcome pinOutcome = pinGuard.check(source, pin, now);
        if (pinOutcome != null) {
            return result(pinOutcome, source);
        }
        if (recipientId == null) {
            return result(Outcome.RECIPIENT_NOT_FOUND, source);
        }
        if (recipientId.equals(sourceId)) {
            return result(Outcome.SAME_ACCOUNT, source);
        }
        if (ledger.existsByAccountIdAndRequestId(sourceId, requestId)) {
            return result(Outcome.DUPLICATE, source);
        }
        if (overLimit(value)) {
            return result(Outcome.OVER_LIMIT, source);
        }
        if (!source.canCover(value)) {
            return result(Outcome.INSUFFICIENT_FUNDS, source);
        }

        source.debit(value);
        recipient.credit(value);
        record(source, EntryType.TRANSFER_OUT, value, recipient.getCardNumber(), requestId, now);
        record(recipient, EntryType.TRANSFER_IN, value, source.getCardNumber(), requestId, now);
        return result(Outcome.OK, source);
    }

    private Account lock(Long accountId) {
        return accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalStateException("Account " + accountId + " does not exist"));
    }

    private boolean overLimit(BigDecimal value) {
        return value.compareTo(properties.limits().maxTransaction()) > 0;
    }

    private void record(Account account, EntryType type, BigDecimal amount, String counterpart,
                        UUID requestId, Instant now) {
        ledger.save(new LedgerEntry(account.getId(), type, amount, account.getBalance(),
                counterpart, requestId, now));
    }

    private static OperationResult result(Outcome outcome, Account account) {
        return new OperationResult(outcome, account.getBalance());
    }

    /** Amounts must be positive and have at most two decimal places. */
    static BigDecimal normalise(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
