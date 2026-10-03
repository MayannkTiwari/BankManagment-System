package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.support.CardNumbers;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final AccountRepository accounts;
    private final PinGuard pinGuard;
    private final Clock clock;

    public LoginAttemptService(AccountRepository accounts, PinGuard pinGuard, Clock clock) {
        this.accounts = accounts;
        this.pinGuard = pinGuard;
        this.clock = clock;
    }

    /** Called after a wrong PIN at sign-in. Unknown card numbers are ignored. */
    @Transactional
    public void recordFailure(String rawCardNumber) {
        String cardNumber = CardNumbers.normalize(rawCardNumber);
        if (cardNumber.isEmpty()) {
            return;
        }
        accounts.findByCardNumberForUpdate(cardNumber)
                .ifPresent(account -> pinGuard.registerFailure(account, Instant.now(clock)));
    }

    @Transactional
    public void reset(Long accountId) {
        accounts.findByIdForUpdate(accountId).ifPresent(account -> account.clearFailedAttempts());
    }
}
