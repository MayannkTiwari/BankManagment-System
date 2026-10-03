package dev.mayanktiwari.bank.service;

import static dev.mayanktiwari.bank.service.Fixtures.OTHER_PIN;
import static dev.mayanktiwari.bank.service.Fixtures.PIN;
import static dev.mayanktiwari.bank.service.Fixtures.amount;
import static dev.mayanktiwari.bank.service.Fixtures.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.domain.EntryType;
import dev.mayanktiwari.bank.domain.LedgerEntry;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BankingServiceTest {

    private final Fixtures f = new Fixtures();

    // ---- deposit ----

    @Test
    void depositIncreasesBalanceAndWritesOneLedgerRow() {
        Account account = f.openAccount("100.00");
        OperationResult result = f.banking.deposit(account.getId(), amount("250.50"), id());

        assertEquals(Outcome.OK, result.outcome());
        assertEquals(0, amount("350.50").compareTo(account.getBalance()));
        assertEquals(1, f.entriesFor(account).size());
        LedgerEntry entry = f.entriesFor(account).get(0);
        assertEquals(EntryType.DEPOSIT, entry.getEntryType());
        assertEquals(0, amount("350.50").compareTo(entry.getBalanceAfter()));
    }

    @Test
    void depositAboveTheLimitIsRefused() {
        Account account = f.openAccount("0");
        OperationResult result = f.banking.deposit(account.getId(), amount("100000.01"), id());
        assertEquals(Outcome.OVER_LIMIT, result.outcome());
        assertEquals(0, amount("0").compareTo(account.getBalance()));
        assertTrue(f.entriesFor(account).isEmpty());
    }

    @Test
    void depositAtExactlyTheLimitIsAccepted() {
        Account account = f.openAccount("0");
        assertEquals(Outcome.OK, f.banking.deposit(account.getId(), amount("100000.00"), id()).outcome());
    }

    @Test
    void sameRequestIdIsAppliedOnlyOnce() {
        Account account = f.openAccount("0");
        UUID request = id();
        assertEquals(Outcome.OK, f.banking.deposit(account.getId(), amount("10"), request).outcome());
        assertEquals(Outcome.DUPLICATE, f.banking.deposit(account.getId(), amount("10"), request).outcome());
        assertEquals(0, amount("10").compareTo(account.getBalance()));
        assertEquals(1, f.entriesFor(account).size());
    }

    @Test
    void zeroNegativeAndTooPreciseAmountsAreProgrammingErrors() {
        Account account = f.openAccount("0");
        assertThrows(IllegalArgumentException.class, () -> f.banking.deposit(account.getId(), amount("0"), id()));
        assertThrows(IllegalArgumentException.class, () -> f.banking.deposit(account.getId(), amount("-5"), id()));
        assertThrows(ArithmeticException.class, () -> f.banking.deposit(account.getId(), amount("1.005"), id()));
    }

    // ---- withdraw ----

    @Test
    void withdrawalReducesBalance() {
        Account account = f.openAccount("500.00");
        OperationResult result = f.banking.withdraw(account.getId(), amount("120.25"), PIN, id());
        assertEquals(Outcome.OK, result.outcome());
        assertEquals(0, amount("379.75").compareTo(account.getBalance()));
        assertEquals(EntryType.WITHDRAWAL, f.entriesFor(account).get(0).getEntryType());
    }

    @Test
    void withdrawingTheWholeBalanceIsAllowed() {
        Account account = f.openAccount("75.00");
        assertEquals(Outcome.OK, f.banking.withdraw(account.getId(), amount("75.00"), PIN, id()).outcome());
        assertEquals(0, amount("0").compareTo(account.getBalance()));
    }

    @Test
    void withdrawalBeyondTheBalanceIsRefusedAndChangesNothing() {
        Account account = f.openAccount("50.00");
        OperationResult result = f.banking.withdraw(account.getId(), amount("50.01"), PIN, id());
        assertEquals(Outcome.INSUFFICIENT_FUNDS, result.outcome());
        assertEquals(0, amount("50.00").compareTo(account.getBalance()));
        assertTrue(f.entriesFor(account).isEmpty());
    }

    @Test
    void wrongPinIsRefusedAndCounted() {
        Account account = f.openAccount("500.00");
        OperationResult result = f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id());
        assertEquals(Outcome.WRONG_PIN, result.outcome());
        assertEquals(0, amount("500.00").compareTo(account.getBalance()));
        assertEquals(1, account.getFailedAttempts());
    }

    @Test
    void fiveWrongPinsLockTheAccountEvenForTheCorrectPin() {
        Account account = f.openAccount("500.00");
        for (int i = 1; i <= 4; i++) {
            assertEquals(Outcome.WRONG_PIN, f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id()).outcome());
        }
        assertEquals(Outcome.LOCKED, f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id()).outcome());
        assertEquals(Outcome.LOCKED, f.banking.withdraw(account.getId(), amount("10"), PIN, id()).outcome());
        assertEquals(0, amount("500.00").compareTo(account.getBalance()));
    }

    @Test
    void accountUnlocksAfterTheLockPeriod() {
        Account account = f.openAccount("500.00");
        for (int i = 0; i < 5; i++) {
            f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id());
        }
        f.clock.advanceMinutes(14);
        assertEquals(Outcome.LOCKED, f.banking.withdraw(account.getId(), amount("10"), PIN, id()).outcome());
        f.clock.advanceMinutes(2);
        assertEquals(Outcome.OK, f.banking.withdraw(account.getId(), amount("10"), PIN, id()).outcome());
    }

    @Test
    void correctPinResetsTheFailureCount() {
        Account account = f.openAccount("500.00");
        f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id());
        f.banking.withdraw(account.getId(), amount("10"), OTHER_PIN, id());
        assertEquals(Outcome.OK, f.banking.withdraw(account.getId(), amount("10"), PIN, id()).outcome());
        assertEquals(0, account.getFailedAttempts());
    }

    @Test
    void withdrawalWithMissingPinCountsAsWrong() {
        Account account = f.openAccount("500.00");
        assertEquals(Outcome.WRONG_PIN, f.banking.withdraw(account.getId(), amount("10"), null, id()).outcome());
    }

    // ---- transfer ----

    @Test
    void transferMovesMoneyAndWritesBothSides() {
        Account sender = f.openAccount("1000.00");
        Account receiver = f.openAccount("20.00");
        UUID request = id();

        OperationResult result = f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("300.00"), PIN, request);

        assertEquals(Outcome.OK, result.outcome());
        assertEquals(0, amount("700.00").compareTo(sender.getBalance()));
        assertEquals(0, amount("320.00").compareTo(receiver.getBalance()));
        LedgerEntry out = f.entriesFor(sender).get(0);
        LedgerEntry in = f.entriesFor(receiver).get(0);
        assertEquals(EntryType.TRANSFER_OUT, out.getEntryType());
        assertEquals(EntryType.TRANSFER_IN, in.getEntryType());
        assertEquals(receiver.getCardNumber(), out.getCounterpartCard());
        assertEquals(sender.getCardNumber(), in.getCounterpartCard());
        assertEquals(0, out.getAmount().compareTo(in.getAmount()));
    }

    @Test
    void totalMoneyIsConservedAcrossManyTransfers() {
        Account a = f.openAccount("1000.00");
        Account b = f.openAccount("1000.00");
        for (int i = 0; i < 20; i++) {
            f.banking.transfer(a.getId(), b.getCardNumber(), amount("33.33"), PIN, id());
            f.banking.transfer(b.getId(), a.getCardNumber(), amount("12.10"), PIN, id());
        }
        assertEquals(0, amount("2000.00").compareTo(a.getBalance().add(b.getBalance())));
    }

    @Test
    void transferWorksInBothIdOrders() {
        Account low = f.openAccount("500.00");
        Account high = f.openAccount("500.00");
        assertEquals(Outcome.OK, f.banking.transfer(low.getId(), high.getCardNumber(), amount("10"), PIN, id()).outcome());
        assertEquals(Outcome.OK, f.banking.transfer(high.getId(), low.getCardNumber(), amount("10"), PIN, id()).outcome());
    }

    @Test
    void transferWithInsufficientFundsChangesNothing() {
        Account sender = f.openAccount("10.00");
        Account receiver = f.openAccount("0");
        assertEquals(Outcome.INSUFFICIENT_FUNDS,
                f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("10.01"), PIN, id()).outcome());
        assertEquals(0, amount("10.00").compareTo(sender.getBalance()));
        assertEquals(0, amount("0").compareTo(receiver.getBalance()));
        assertTrue(f.ledgerRows.isEmpty());
    }

    @Test
    void transferToUnknownCardIsRefused() {
        Account sender = f.openAccount("100.00");
        OperationResult result = f.banking.transfer(sender.getId(), "9000000000000000", amount("10"), PIN, id());
        assertEquals(Outcome.RECIPIENT_NOT_FOUND, result.outcome());
        assertEquals(0, amount("100.00").compareTo(sender.getBalance()));
    }

    @Test
    void unknownRecipientIsNotRevealedWithoutTheCorrectPin() {
        Account sender = f.openAccount("100.00");
        OperationResult result = f.banking.transfer(sender.getId(), "9000000000000000", amount("10"), OTHER_PIN, id());
        assertEquals(Outcome.WRONG_PIN, result.outcome());
    }

    @Test
    void transferToYourOwnAccountIsRefused() {
        Account sender = f.openAccount("100.00");
        assertEquals(Outcome.SAME_ACCOUNT,
                f.banking.transfer(sender.getId(), sender.getCardNumber(), amount("10"), PIN, id()).outcome());
        assertEquals(0, amount("100.00").compareTo(sender.getBalance()));
    }

    @Test
    void transferWithWrongPinIsRefusedAndCounted() {
        Account sender = f.openAccount("100.00");
        Account receiver = f.openAccount("0");
        OperationResult result = f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("10"), OTHER_PIN, id());
        assertEquals(Outcome.WRONG_PIN, result.outcome());
        assertEquals(1, sender.getFailedAttempts());
        assertEquals(0, amount("0").compareTo(receiver.getBalance()));
    }

    @Test
    void duplicateTransferRequestIsAppliedOnce() {
        Account sender = f.openAccount("100.00");
        Account receiver = f.openAccount("0");
        UUID request = id();
        assertEquals(Outcome.OK, f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("40"), PIN, request).outcome());
        assertEquals(Outcome.DUPLICATE, f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("40"), PIN, request).outcome());
        assertEquals(0, amount("60").compareTo(sender.getBalance()));
        assertEquals(0, amount("40").compareTo(receiver.getBalance()));
    }

    @Test
    void transferOverTheLimitIsRefused() {
        Account sender = f.openAccount("0");
        sender.credit(amount("500000"));
        Account receiver = f.openAccount("0");
        assertEquals(Outcome.OVER_LIMIT,
                f.banking.transfer(sender.getId(), receiver.getCardNumber(), amount("100000.01"), PIN, id()).outcome());
    }
}
