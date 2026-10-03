package dev.mayanktiwari.bank.service;

import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.support.Pins;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PinService {

    private final AccountRepository accounts;
    private final PinGuard pinGuard;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public PinService(AccountRepository accounts, PinGuard pinGuard, PasswordEncoder encoder, Clock clock) {
        this.accounts = accounts;
        this.pinGuard = pinGuard;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Transactional
    public Outcome changePin(Long accountId, String currentPin, String newPin) {
        if (!Pins.isAcceptable(newPin)) {
            throw new IllegalArgumentException("The new PIN does not meet the PIN rules");
        }
        Account account = accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalStateException("Account " + accountId + " does not exist"));

        Outcome pinOutcome = pinGuard.check(account, currentPin, Instant.now(clock));
        if (pinOutcome != null) {
            return pinOutcome;
        }
        if (encoder.matches(newPin, account.getPinHash())) {
            return Outcome.SAME_PIN;
        }
        account.changePinHash(encoder.encode(newPin));
        return Outcome.OK;
    }
}
