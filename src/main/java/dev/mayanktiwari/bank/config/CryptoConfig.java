package dev.mayanktiwari.bank.config;

import dev.mayanktiwari.bank.security.PepperedPinEncoder;
import dev.mayanktiwari.bank.support.DataCipher;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Kept apart from SecurityConfig on purpose: services depend on the PasswordEncoder, and the
 * security configuration depends on those services, so defining it there would be circular.
 */
@Configuration
public class CryptoConfig {

    @Bean
    public DataCipher dataCipher(AppProperties properties) {
        return new DataCipher(properties.security().dataKey());
    }

    @Bean
    public PasswordEncoder passwordEncoder(AppProperties properties) {
        return new PepperedPinEncoder(properties.security().pinPepper());
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
