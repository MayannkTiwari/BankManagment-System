package dev.mayanktiwari.bank.service;

import java.math.BigDecimal;

public record OperationResult(Outcome outcome, BigDecimal balance) {

    public boolean ok() {
        return outcome == Outcome.OK;
    }
}
