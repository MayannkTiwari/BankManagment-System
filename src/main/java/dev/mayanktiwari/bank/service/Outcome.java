package dev.mayanktiwari.bank.service;

/**
 * Result of an operation that can fail for ordinary reasons. These are returned instead of thrown
 * so that the transaction commits, which matters because a wrong PIN has to be counted even though
 * the money movement did not happen.
 */
public enum Outcome {
    OK,
    DUPLICATE,
    WRONG_PIN,
    LOCKED,
    INSUFFICIENT_FUNDS,
    OVER_LIMIT,
    RECIPIENT_NOT_FOUND,
    SAME_ACCOUNT,
    SAME_PIN
}
