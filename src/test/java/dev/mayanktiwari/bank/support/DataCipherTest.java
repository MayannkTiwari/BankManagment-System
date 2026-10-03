package dev.mayanktiwari.bank.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class DataCipherTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OTHER_KEY = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    @Test
    void roundTrips() {
        DataCipher cipher = new DataCipher(KEY);
        String token = cipher.encrypt("ABCDE1234F");
        assertEquals("ABCDE1234F", cipher.decrypt(token));
        assertFalse(token.contains("ABCDE"));
    }

    @Test
    void sameInputGivesDifferentCiphertext() {
        DataCipher cipher = new DataCipher(KEY);
        assertNotEquals(cipher.encrypt("ABCDE1234F"), cipher.encrypt("ABCDE1234F"));
    }

    @Test
    void tamperedOrForeignCiphertextIsRejected() {
        DataCipher cipher = new DataCipher(KEY);
        String token = cipher.encrypt("ABCDE1234F");
        byte[] raw = Base64.getDecoder().decode(token);
        raw[raw.length - 1] ^= 1;
        String tampered = Base64.getEncoder().encodeToString(raw);
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));
        assertThrows(IllegalStateException.class, () -> new DataCipher(OTHER_KEY).decrypt(token));
    }

    @Test
    void rejectsKeysOfTheWrongSize() {
        assertThrows(IllegalArgumentException.class, () -> new DataCipher(""));
        assertThrows(IllegalArgumentException.class,
                () -> new DataCipher(Base64.getEncoder().encodeToString(new byte[16])));
        assertThrows(IllegalArgumentException.class, () -> new DataCipher("not base64 !!"));
    }
}
