package dev.mayanktiwari.bank.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Withdrawal form: an amount confirmed with the PIN. */
public class PinAmountForm extends AmountForm {

    @NotBlank(message = "Enter your 4-digit PIN")
    @Pattern(regexp = "^(?:[0-9]{4})?$", message = "Enter your 4-digit PIN")
    private String pin;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
