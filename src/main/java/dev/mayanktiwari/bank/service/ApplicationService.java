package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.AccountType;
import dev.mayanktiwari.bank.domain.Customer;
import dev.mayanktiwari.bank.domain.Gender;
import dev.mayanktiwari.bank.domain.IncomeRange;
import dev.mayanktiwari.bank.domain.MaritalStatus;
import dev.mayanktiwari.bank.domain.Occupation;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.repository.CustomerRepository;
import dev.mayanktiwari.bank.support.CardNumbers;
import dev.mayanktiwari.bank.support.DataCipher;
import dev.mayanktiwari.bank.support.Pins;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    /** Validated and normalised application data. The Aadhaar number is passed in full and reduced here. */
    public record NewApplication(
            String fullName,
            String fatherName,
            LocalDate dateOfBirth,
            Gender gender,
            String email,
            MaritalStatus maritalStatus,
            String address,
            String city,
            String pinCode,
            Occupation occupation,
            IncomeRange incomeRange,
            String pan,
            String aadhaar,
            AccountType accountType) {
    }

    /** Shown to the applicant once. The PIN cannot be recovered afterwards. */
    public record OpenedAccount(String cardNumber, String pin, AccountType accountType) {
    }

    private static final int MAX_CARD_ATTEMPTS = 10;

    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final DataCipher cipher;
    private final Clock clock;

    public ApplicationService(CustomerRepository customers, AccountRepository accounts,
                              PasswordEncoder encoder, DataCipher cipher, Clock clock) {
        this.customers = customers;
        this.accounts = accounts;
        this.encoder = encoder;
        this.cipher = cipher;
        this.clock = clock;
    }

    @Transactional
    public OpenedAccount open(NewApplication application) {
        Instant now = Instant.now(clock);
        String aadhaar = application.aadhaar();

        Customer customer = customers.save(new Customer(
                application.fullName(),
                application.fatherName(),
                application.dateOfBirth(),
                application.gender(),
                application.email(),
                application.maritalStatus(),
                application.address(),
                application.city(),
                application.pinCode(),
                application.occupation(),
                application.incomeRange(),
                cipher.encrypt(application.pan()),
                aadhaar.substring(aadhaar.length() - 4),
                now));

        String cardNumber = uniqueCardNumber();
        String pin = Pins.generate();
        accounts.save(new Account(customer, application.accountType(), cardNumber, encoder.encode(pin), now));

        return new OpenedAccount(cardNumber, pin, application.accountType());
    }

    private String uniqueCardNumber() {
        for (int i = 0; i < MAX_CARD_ATTEMPTS; i++) {
            String candidate = CardNumbers.generate();
            if (!accounts.existsByCardNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique card number");
    }
}
