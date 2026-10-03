package dev.mayanktiwari.bank.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangePinForm {

    @NotBlank(message = "Enter your current PIN")
    @Pattern(regexp = "^(?:[0-9]{4})?$", message = "Your PIN is 4 digits")
    private String currentPin;

    @NotBlank(message = "Enter a new PIN")
    @Pattern(regexp = "^(?:[0-9]{4})?$", message = "The new PIN must be 4 digits")
    private String newPin;

    @NotBlank(message = "Enter the new PIN again")
    private String confirmPin;

    public String getCurrentPin() {
        return currentPin;
    }

    public void setCurrentPin(String currentPin) {
        this.currentPin = currentPin;
    }

    public String getNewPin() {
        return newPin;
    }

    public void setNewPin(String newPin) {
        this.newPin = newPin;
    }

    public String getConfirmPin() {
        return confirmPin;
    }

    public void setConfirmPin(String confirmPin) {
        this.confirmPin = confirmPin;
    }
}
