package dev.mayanktiwari.bank.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A 4-digit PIN has only 10,000 possible values, so a plain bcrypt hash could be reversed in
 * seconds by anyone who obtains the database. The PIN is first run through HMAC-SHA256 with a
 * secret pepper that lives in the environment, not in the database, and the result is hashed
 * with bcrypt. A database leak alone is then not enough to recover PINs.
 */
public class PepperedPinEncoder implements PasswordEncoder {

    private static final String HMAC = "HmacSHA256";

    private static final int DEFAULT_COST = 12;

    private final BCryptPasswordEncoder bcrypt;
    private final byte[] pepper;

    public PepperedPinEncoder(String pepper) {
        this(pepper, DEFAULT_COST);
    }

    /** The cost is a parameter only so that unit tests can use the cheapest value. */
    public PepperedPinEncoder(String pepper, int cost) {
        if (pepper == null || pepper.length() < 32) {
            throw new IllegalArgumentException("The PIN pepper must be at least 32 characters");
        }
        this.pepper = pepper.getBytes(StandardCharsets.UTF_8);
        this.bcrypt = new BCryptPasswordEncoder(cost);
    }

    @Override
    public String encode(CharSequence rawPin) {
        return bcrypt.encode(prehash(rawPin));
    }

    @Override
    public boolean matches(CharSequence rawPin, String encoded) {
        if (rawPin == null || encoded == null || encoded.isEmpty()) {
            return false;
        }
        return bcrypt.matches(prehash(rawPin), encoded);
    }

    private String prehash(CharSequence rawPin) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(pepper, HMAC));
            byte[] digest = mac.doFinal(rawPin.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}
