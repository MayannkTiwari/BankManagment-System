package dev.mayanktiwari.bank.support;

import java.security.SecureRandom;

/**
 * Generation and validation of the 16-digit card number used to sign in.
 * Numbers start with 9, which is not used by any of the major card networks,
 * and end with a Luhn check digit so that typing mistakes can be caught early.
 */
public final class CardNumbers {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CardNumbers() {
    }

    public static String generate() {
        StringBuilder number = new StringBuilder(16);
        number.append('9');
        for (int i = 0; i < 14; i++) {
            number.append(RANDOM.nextInt(10));
        }
        number.append(luhnCheckDigit(number));
        return number.toString();
    }

    public static boolean isValid(String number) {
        if (number == null || number.length() != 16) {
            return false;
        }
        for (int i = 0; i < 16; i++) {
            char c = number.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return luhnCheckDigit(number.substring(0, 15)) == number.charAt(15) - '0';
    }

    /** Removes spaces and hyphens so "9123 4567 ..." and "9123-4567-..." are accepted. */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[\\s-]", "");
    }

    /** Formats a 16-digit number as four groups of four. */
    public static String grouped(String number) {
        if (number == null || number.length() != 16) {
            return number == null ? "" : number;
        }
        return number.substring(0, 4) + " " + number.substring(4, 8) + " "
                + number.substring(8, 12) + " " + number.substring(12);
    }

    public static String lastFour(String number) {
        if (number == null || number.length() < 4) {
            return "";
        }
        return number.substring(number.length() - 4);
    }

    static int luhnCheckDigit(CharSequence payload) {
        int sum = 0;
        boolean doubleIt = true;
        for (int i = payload.length() - 1; i >= 0; i--) {
            int digit = payload.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return (10 - sum % 10) % 10;
    }
}
