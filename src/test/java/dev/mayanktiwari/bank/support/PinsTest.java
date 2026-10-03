package dev.mayanktiwari.bank.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PinsTest {

    @Test
    void rejectsRepeatedAndSequentialPins() {
        for (String pin : new String[] {"0000", "1111", "9999", "1234", "2345", "6789", "4321", "9876", "3210"}) {
            assertFalse(Pins.isAcceptable(pin), pin);
        }
    }

    @Test
    void rejectsMalformedPins() {
        for (String pin : new String[] {null, "", "123", "12345", "12a4", " 123"}) {
            assertFalse(Pins.isAcceptable(pin), String.valueOf(pin));
        }
    }

    @Test
    void acceptsOrdinaryPins() {
        for (String pin : new String[] {"1357", "4829", "0721", "1212", "8080"}) {
            assertTrue(Pins.isAcceptable(pin), pin);
        }
    }

    @Test
    void generatedPinsAreAlwaysAcceptable() {
        for (int i = 0; i < 2000; i++) {
            assertTrue(Pins.isAcceptable(Pins.generate()));
        }
    }
}
