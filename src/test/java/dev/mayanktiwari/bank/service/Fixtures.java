package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.AccountType;
import dev.mayanktiwari.bank.domain.Customer;
import dev.mayanktiwari.bank.domain.Gender;
import dev.mayanktiwari.bank.domain.IncomeRange;
import dev.mayanktiwari.bank.domain.LedgerEntry;
import dev.mayanktiwari.bank.domain.MaritalStatus;
import dev.mayanktiwari.bank.domain.Occupation;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.repository.CustomerRepository;
import dev.mayanktiwari.bank.repository.LedgerEntryRepository;
import dev.mayanktiwari.bank.security.PepperedPinEncoder;
import dev.mayanktiwari.bank.support.CardNumbers;
import dev.mayanktiwari.bank.support.DataCipher;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory stand-ins for the repositories, built with java.lang.reflect.Proxy so that only the
 * methods the services really call have to be provided. There is no database and no Spring here:
 * these tests check the banking rules, not the persistence mapping.
 */
final class Fixtures {

    static final String PIN = "4829";
    static final String OTHER_PIN = "1357";

    final Map<Long, Account> accounts = new LinkedHashMap<>();
    final Map<Long, Customer> customers = new LinkedHashMap<>();
    final List<LedgerEntry> ledgerRows = new ArrayList<>();

    final MutableClock clock = new MutableClock(Instant.parse("2026-10-03T10:00:00Z"));
    final AppProperties properties = new AppProperties(
            "Bank", "Operator", "help@example.com", "Delhi", "https://bank.example.com", true, "Asia/Kolkata",
            new AppProperties.Security(Base64.getEncoder().encodeToString(new byte[32]), "p".repeat(40), 5, 15),
            new AppProperties.Limits(new BigDecimal("100000.00")));
    final PepperedPinEncoder encoder = new PepperedPinEncoder("p".repeat(40), 4);
    final DataCipher cipher = new DataCipher(Base64.getEncoder().encodeToString(new byte[32]));

    final AccountRepository accountRepository = proxy(AccountRepository.class, (name, args) -> switch (name) {
        case "findByIdForUpdate", "findById" -> Optional.ofNullable(accounts.get((Long) args[0]));
        case "findByCardNumber", "findByCardNumberForUpdate" -> accounts.values().stream()
                .filter(a -> a.getCardNumber().equals(args[0])).findFirst();
        case "existsByCardNumber" -> accounts.values().stream().anyMatch(a -> a.getCardNumber().equals(args[0]));
        case "save" -> {
            Account account = (Account) args[0];
            assignId(account, accounts.size() + 1L);
            accounts.put(account.getId(), account);
            yield account;
        }
        default -> throw new UnsupportedOperationException(name);
    });

    final CustomerRepository customerRepository = proxy(CustomerRepository.class, (name, args) -> {
        if (name.equals("save")) {
            Customer customer = (Customer) args[0];
            assignId(customer, customers.size() + 1L);
            customers.put(customer.getId(), customer);
            return customer;
        }
        throw new UnsupportedOperationException(name);
    });

    final LedgerEntryRepository ledgerRepository = proxy(LedgerEntryRepository.class, (name, args) -> switch (name) {
        case "existsByAccountIdAndRequestId" -> ledgerRows.stream()
                .anyMatch(e -> e.getAccountId().equals(args[0]) && e.getRequestId().equals(args[1]));
        case "save" -> {
            LedgerEntry entry = (LedgerEntry) args[0];
            assignId(entry, ledgerRows.size() + 1L);
            ledgerRows.add(entry);
            yield entry;
        }
        default -> throw new UnsupportedOperationException(name);
    });

    final PinGuard pinGuard = new PinGuard(encoder, properties);
    final BankingService banking = new BankingService(accountRepository, ledgerRepository, pinGuard, properties, clock);
    final PinService pinService = new PinService(accountRepository, pinGuard, encoder, clock);
    final LoginAttemptService loginAttempts = new LoginAttemptService(accountRepository, pinGuard, clock);
    final ApplicationService applications =
            new ApplicationService(customerRepository, accountRepository, encoder, cipher, clock);

    /** Creates an account with the given opening balance and PIN 4829. */
    Account openAccount(String balance) {
        Customer customer = customerRepository.save(new Customer("Test Person", "Test Father",
                LocalDate.of(1999, 1, 1), Gender.OTHER, "test@example.com", MaritalStatus.UNMARRIED,
                "1 Test Road", "Delhi", "110001", Occupation.STUDENT, IncomeRange.NO_INCOME,
                cipher.encrypt("ABCDE1234F"), "9012", clock.instant()));
        Account account = accountRepository.save(new Account(customer, AccountType.SAVINGS,
                CardNumbers.generate(), encoder.encode(PIN), clock.instant()));
        if (new BigDecimal(balance).signum() > 0) {
            account.credit(new BigDecimal(balance));
        }
        return account;
    }

    List<LedgerEntry> entriesFor(Account account) {
        return ledgerRows.stream().filter(e -> e.getAccountId().equals(account.getId())).toList();
    }

    static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    static UUID id() {
        return UUID.randomUUID();
    }

    private static void assignId(Object entity, long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, id);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private interface Handler {
        Object handle(String method, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> handler.handle(method.getName(), args == null ? new Object[0] : args));
    }

    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advanceMinutes(long minutes) {
            now = now.plusSeconds(minutes * 60);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
