package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.domain.Account;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Checks a PIN against an account that the caller has already locked for update, and keeps the
 * failed-attempt counter. Used for sign-in failures, withdrawals, transfers and PIN changes.
 */
@Component
public class PinGuard {

    private final PasswordEncoder encoder;
    private final AppProperties properties;

    public PinGuard(PasswordEncoder encoder, AppProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    /** Returns null when the PIN is correct, otherwise LOCKED or WRONG_PIN. */
    public Outcome check(Account account, String pin, Instant now) {
        if (account.isLocked(now)) {
            return Outcome.LOCKED;
        }
        if (pin != null && encoder.matches(pin, account.getPinHash())) {
            account.clearFailedAttempts();
            return null;
        }
        registerFailure(account, now);
        return account.isLocked(now) ? Outcome.LOCKED : Outcome.WRONG_PIN;
    }

    public void registerFailure(Account account, Instant now) {
        account.registerFailedAttempt(
                properties.security().maxFailedAttempts(), properties.lockDuration(), now);
    }
}
