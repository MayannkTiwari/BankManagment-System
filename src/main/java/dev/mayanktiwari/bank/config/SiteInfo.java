package dev.mayanktiwari.bank.config;

/** The subset of configuration that templates are allowed to see. */
public record SiteInfo(
        String brandName,
        String operatorName,
        String contactEmail,
        String jurisdiction,
        boolean demoMode) {

    public static SiteInfo from(AppProperties p) {
        return new SiteInfo(p.brandName(), p.operatorName(), p.contactEmail(), p.jurisdiction(), p.demoMode());
    }
}
