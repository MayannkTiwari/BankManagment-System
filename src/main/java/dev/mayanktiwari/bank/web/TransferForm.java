package dev.mayanktiwari.bank.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TransferForm extends PinAmountForm {

    @NotBlank(message = "Enter the recipient's card number")
    @Size(max = 23, message = "Enter a 16-digit card number")
    private String recipientCard;

    public String getRecipientCard() {
        return recipientCard;
    }

    public void setRecipientCard(String recipientCard) {
        this.recipientCard = recipientCard;
    }
}
