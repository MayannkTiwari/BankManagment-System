package dev.mayanktiwari.bank.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Everything configurable lives under the "app" prefix. Values without a default in
 * application.yml must be supplied through environment variables, so the application
 * refuses to start with missing secrets or missing legal details.
 */
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank String brandName,
        @NotBlank String operatorName,
        @NotBlank String contactEmail,
        @NotBlank String jurisdiction,
        @NotBlank String publicUrl,
        boolean demoMode,
        @NotBlank String timezone,
        @Valid @NotNull Security security,
        @Valid @NotNull Limits limits) {

    public record Security(
            @NotBlank String dataKey,
            @NotBlank @Size(min = 32, message = "APP_PIN_PEPPER must be at least 32 characters") String pinPepper,
            @Min(1) int maxFailedAttempts,
            @Min(1) int lockMinutes) {
    }

    public record Limits(@NotNull @DecimalMin("1.00") BigDecimal maxTransaction) {
    }

    public ZoneId zone() {
        return ZoneId.of(timezone);
    }

    public Duration lockDuration() {
        return Duration.ofMinutes(security.lockMinutes());
    }
}
