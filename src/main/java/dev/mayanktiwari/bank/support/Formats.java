package dev.mayanktiwari.bank.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Display formatting. Amounts use Indian digit grouping, for example 12,34,567.89. */
public final class Formats {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private Formats() {
    }

    public static String rupees(BigDecimal amount) {
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        String sign = scaled.signum() < 0 ? "-" : "";
        String plain = scaled.abs().toPlainString();
        int dot = plain.indexOf('.');
        return sign + "\u20B9" + groupIndian(plain.substring(0, dot)) + plain.substring(dot);
    }

    /** Amount without the currency sign, for table columns. */
    public static String plainAmount(BigDecimal amount) {
        return rupees(amount).replace("\u20B9", "");
    }

    public static String dateTime(Instant instant, ZoneId zone) {
        return DATE_TIME.format(instant.atZone(zone));
    }

    public static String date(Instant instant, ZoneId zone) {
        return DATE.format(instant.atZone(zone));
    }

    public static String date(LocalDate date) {
        return DATE.format(date);
    }

    static String groupIndian(String digits) {
        if (digits.length() <= 3) {
            return digits;
        }
        String lastThree = digits.substring(digits.length() - 3);
        String rest = digits.substring(0, digits.length() - 3);
        StringBuilder out = new StringBuilder();
        int firstGroup = rest.length() % 2;
        if (firstGroup > 0) {
            out.append(rest, 0, firstGroup);
        }
        for (int i = firstGroup; i < rest.length(); i += 2) {
            if (out.length() > 0) {
                out.append(',');
            }
            out.append(rest, i, i + 2);
        }
        return out + "," + lastThree;
    }
}
