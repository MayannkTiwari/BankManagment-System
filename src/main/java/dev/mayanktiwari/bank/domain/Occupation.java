package dev.mayanktiwari.bank.domain;

public enum Occupation {
    SALARIED("Salaried"),
    GOVERNMENT("Government job"),
    DEFENCE("Defence"),
    SELF_EMPLOYED("Self-employed"),
    STUDENT("Student"),
    OTHER("Other");

    private final String label;

    Occupation(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
