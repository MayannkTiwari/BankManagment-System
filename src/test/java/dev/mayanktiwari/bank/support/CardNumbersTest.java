package dev.mayanktiwari.bank.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardNumbersTest {

    @Test
    void luhnCheckDigitMatchesPublishedExample() {
        // 79927398713 is the standard Luhn example: payload 7992739871, check digit 3
        assertEquals(3, CardNumbers.luhnCheckDigit("7992739871"));
    }

    @Test
    void generatedNumbersAreSixteenDigitsAndValid() {
        for (int i = 0; i < 500; i++) {
            String number = CardNumbers.generate();
            assertEquals(16, number.length());
            assertTrue(number.startsWith("9"));
            assertTrue(CardNumbers.isValid(number), number);
        }
    }

    @Test
    void rejectsWrongLengthLettersAndBadCheckDigit() {
        String valid = CardNumbers.generate();
        int wrongCheck = (valid.charAt(15) - '0' + 1) % 10;
        assertFalse(CardNumbers.isValid(valid.substring(0, 15) + wrongCheck));
        assertFalse(CardNumbers.isValid(valid.substring(0, 15)));
        assertFalse(CardNumbers.isValid("abcdefghijklmnop"));
        assertFalse(CardNumbers.isValid(null));
        assertFalse(CardNumbers.isValid(""));
    }

    @Test
    void normalizeRemovesSpacesAndHyphens() {
        assertEquals("9123456789012345", CardNumbers.normalize(" 9123 4567-8901 2345 "));
        assertEquals("", CardNumbers.normalize(null));
    }

    @Test
    void groupsAndLastFour() {
        assertEquals("9123 4567 8901 2345", CardNumbers.grouped("9123456789012345"));
        assertEquals("2345", CardNumbers.lastFour("9123456789012345"));
        assertEquals("", CardNumbers.lastFour(null));
    }
}
