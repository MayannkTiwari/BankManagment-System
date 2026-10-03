package dev.mayanktiwari.bank.domain;

public enum MaritalStatus {
    MARRIED("Married"),
    UNMARRIED("Unmarried"),
    OTHER("Other");

    private final String label;

    MaritalStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
