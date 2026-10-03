package dev.mayanktiwari.bank.domain;

public enum AccountType {
    SAVINGS("Savings account"),
    CURRENT("Current account");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
