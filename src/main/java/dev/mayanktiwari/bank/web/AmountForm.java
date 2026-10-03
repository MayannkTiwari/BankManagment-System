package dev.mayanktiwari.bank.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/** Deposit form. The request id is generated when the page is rendered and makes the submit idempotent. */
public class AmountForm {

    @NotNull(message = "This form has expired. Reload the page and try again")
    private UUID requestId = UUID.randomUUID();

    @NotNull(message = "Enter an amount")
    @DecimalMin(value = "0.01", message = "Enter an amount of at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Enter a valid amount with up to two decimal places")
    private BigDecimal amount;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
