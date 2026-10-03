package dev.mayanktiwari.bank.service;

import static dev.mayanktiwari.bank.service.Fixtures.OTHER_PIN;
import static dev.mayanktiwari.bank.service.Fixtures.PIN;
import static dev.mayanktiwari.bank.service.Fixtures.amount;
import static dev.mayanktiwari.bank.service.Fixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.AccountType;
import dev.mayanktiwari.bank.domain.Customer;
import dev.mayanktiwari.bank.domain.Gender;
import dev.mayanktiwari.bank.domain.IncomeRange;
import dev.mayanktiwari.bank.domain.MaritalStatus;
import dev.mayanktiwari.bank.domain.Occupation;
import dev.mayanktiwari.bank.support.CardNumbers;
import dev.mayanktiwari.bank.support.Pins;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PinAndApplicationServiceTest {

    private final Fixtures f = new Fixtures();

    // ---- PIN change ----

    @Test
    void changingThePinReplacesTheHash() {
        Account account = f.openAccount("0");
        assertEquals(Outcome.OK, f.pinService.changePin(account.getId(), PIN, OTHER_PIN));
        assertTrue(f.encoder.matches(OTHER_PIN, account.getPinHash()));
        assertFalse(f.encoder.matches(PIN, account.getPinHash()));
    }

    @Test
    void changingThePinNeedsTheCurrentPin() {
        Account account = f.openAccount("0");
        assertEquals(Outcome.WRONG_PIN, f.pinService.changePin(account.getId(), "9753", OTHER_PIN));
        assertTrue(f.encoder.matches(PIN, account.getPinHash()));
        assertEquals(1, account.getFailedAttempts());
    }

    @Test
    void newPinMustDifferFromTheCurrentOne() {
        Account account = f.openAccount("0");
        assertEquals(Outcome.SAME_PIN, f.pinService.changePin(account.getId(), PIN, PIN));
    }

    @Test
    void weakNewPinIsRejectedByTheService() {
        Account account = f.openAccount("0");
        assertThrows(IllegalArgumentException.class, () -> f.pinService.changePin(account.getId(), PIN, "1234"));
    }

    @Test
    void lockedAccountCannotChangeThePin() {
        Account account = f.openAccount("0");
        for (int i = 0; i < 5; i++) {
            f.pinService.changePin(account.getId(), "9753", OTHER_PIN);
        }
        assertEquals(Outcome.LOCKED, f.pinService.changePin(account.getId(), PIN, OTHER_PIN));
    }

    // ---- sign-in attempt tracking ----

    @Test
    void failedSignInsLockTheAccountAndSuccessClearsTheCount() {
        Account account = f.openAccount("0");
        f.loginAttempts.recordFailure(" " + CardNumbers.grouped(account.getCardNumber()) + " ");
        f.loginAttempts.recordFailure(account.getCardNumber());
        assertEquals(2, account.getFailedAttempts());
        f.loginAttempts.reset(account.getId());
        assertEquals(0, account.getFailedAttempts());

        for (int i = 0; i < 5; i++) {
            f.loginAttempts.recordFailure(account.getCardNumber());
        }
        assertTrue(account.isLocked(f.clock.instant()));
    }

    @Test
    void failedSignInForUnknownCardIsIgnored() {
        f.openAccount("0");
        f.loginAttempts.recordFailure("9000000000000000");
        f.loginAttempts.recordFailure(null);
        f.loginAttempts.recordFailure("");
    }

    // ---- application ----

    private ApplicationService.NewApplication application(String aadhaar) {
        return new ApplicationService.NewApplication("Asha Verma", "Raj Verma", LocalDate.of(2000, 5, 17),
                Gender.FEMALE, "asha@example.com", MaritalStatus.UNMARRIED, "12 Test Lane", "Noida", "201301",
                Occupation.STUDENT, IncomeRange.NO_INCOME, "ABCDE1234F", aadhaar, AccountType.SAVINGS);
    }

    @Test
    void openingAnAccountIssuesAValidCardNumberAndPin() {
        ApplicationService.OpenedAccount opened = f.applications.open(application("234567890123"));

        assertTrue(CardNumbers.isValid(opened.cardNumber()));
        assertTrue(Pins.isAcceptable(opened.pin()));
        Account account = f.accountRepository.findByCardNumber(opened.cardNumber()).orElseThrow();
        assertEquals(0, amount("0").compareTo(account.getBalance()));
        assertTrue(f.encoder.matches(opened.pin(), account.getPinHash()));
        assertNotEquals(opened.pin(), account.getPinHash());
    }

    @Test
    void panIsEncryptedAndOnlyTheLastFourAadhaarDigitsAreKept() {
        ApplicationService.OpenedAccount opened = f.applications.open(application("234567890123"));
        Customer customer = f.accountRepository.findByCardNumber(opened.cardNumber()).orElseThrow().getCustomer();

        assertNotEquals("ABCDE1234F", customer.getPanEncrypted());
        assertFalse(customer.getPanEncrypted().contains("ABCDE"));
        assertEquals("ABCDE1234F", f.cipher.decrypt(customer.getPanEncrypted()));
        assertEquals("0123", customer.getAadhaarLast4());
    }

    @Test
    void twoApplicationsGetDifferentCardNumbers() {
        String first = f.applications.open(application("234567890123")).cardNumber();
        String second = f.applications.open(application("234567890123")).cardNumber();
        assertNotEquals(first, second);
    }

    @Test
    void newAccountIsUsableStraightAway() {
        ApplicationService.OpenedAccount opened = f.applications.open(application("234567890123"));
        Account account = f.accountRepository.findByCardNumber(opened.cardNumber()).orElseThrow();
        assertEquals(Outcome.OK, f.banking.deposit(account.getId(), amount("500"), id()).outcome());
        assertEquals(Outcome.OK, f.banking.withdraw(account.getId(), amount("100"), opened.pin(), id()).outcome());
        assertEquals(0, amount("400.00").compareTo(account.getBalance()));
    }
}
