package dev.mayanktiwari.bank.domain;

public enum IncomeRange {
    NO_INCOME("No income"),
    UNDER_2_5_LAKH("Below \u20B92,50,000"),
    LAKH_2_5_TO_5("\u20B92,50,000 to \u20B95,00,000"),
    LAKH_5_TO_10("\u20B95,00,000 to \u20B910,00,000"),
    OVER_10_LAKH("Above \u20B910,00,000");

    private final String label;

    IncomeRange(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
