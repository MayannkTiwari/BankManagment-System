package dev.mayanktiwari.bank.web;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.config.SiteInfo;
import dev.mayanktiwari.bank.support.Formats;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page needs: site details for the header and footer, and the published limits. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AppProperties properties;
    private final SiteInfo site;

    public GlobalModelAdvice(AppProperties properties) {
        this.properties = properties;
        this.site = SiteInfo.from(properties);
    }

    @ModelAttribute("site")
    public SiteInfo site() {
        return site;
    }

    @ModelAttribute("year")
    public int year() {
        return LocalDate.now(properties.zone()).getYear();
    }

    @ModelAttribute("signedIn")
    public boolean signedIn() {
        return SignedIn.check();
    }

    @ModelAttribute("maxTransaction")
    public String maxTransaction() {
        return Formats.rupees(properties.limits().maxTransaction());
    }

    @ModelAttribute("maxAttempts")
    public int maxAttempts() {
        return properties.security().maxFailedAttempts();
    }

    @ModelAttribute("lockMinutes")
    public int lockMinutes() {
        return properties.security().lockMinutes();
    }
}
