package dev.mayanktiwari.bank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "card_number", nullable = false, length = 16, updatable = false)
    private String cardNumber;

    @Column(name = "pin_hash", nullable = false, length = 100)
    private String pinHash;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Account() {
    }

    public Account(Customer customer, AccountType accountType, String cardNumber,
                   String pinHash, Instant createdAt) {
        this.customer = customer;
        this.accountType = accountType;
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.balance = new BigDecimal("0.00");
        this.createdAt = createdAt;
    }

    public boolean canCover(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (!canCover(amount)) {
            throw new IllegalStateException("Debit would make the balance negative");
        }
        balance = balance.subtract(amount);
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    /** Counts a wrong PIN. Reaching the limit locks the account and restarts the count. */
    public void registerFailedAttempt(int maxAttempts, Duration lockFor, Instant now) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            lockedUntil = now.plus(lockFor);
            failedAttempts = 0;
        }
    }

    public void clearFailedAttempts() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void changePinHash(String newHash) {
        this.pinHash = newHash;
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPinHash() {
        return pinHash;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
