package dev.mayanktiwari.bank.support;

import java.security.SecureRandom;

/** Rules for the 4-digit PIN. */
public final class Pins {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Pins() {
    }

    /** Generates a random PIN that passes {@link #isAcceptable(String)}. */
    public static String generate() {
        while (true) {
            String candidate = String.format("%04d", RANDOM.nextInt(10_000));
            if (isAcceptable(candidate)) {
                return candidate;
            }
        }
    }

    /** A PIN is four digits, not all the same, and not a run such as 1234 or 8765. */
    public static boolean isAcceptable(String pin) {
        if (pin == null || pin.length() != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            char c = pin.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        boolean allSame = true;
        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < 4; i++) {
            int previous = pin.charAt(i - 1);
            int current = pin.charAt(i);
            if (current != previous) {
                allSame = false;
            }
            if (current != previous + 1) {
                ascending = false;
            }
            if (current != previous - 1) {
                descending = false;
            }
        }
        return !(allSame || ascending || descending);
    }
}
