package dev.mayanktiwari.bank.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class FormatsTest {

    private static final String RUPEE = "\u20B9";

    @Test
    void usesIndianDigitGrouping() {
        assertEquals(RUPEE + "0.00", Formats.rupees(new BigDecimal("0")));
        assertEquals(RUPEE + "999.50", Formats.rupees(new BigDecimal("999.5")));
        assertEquals(RUPEE + "1,000.00", Formats.rupees(new BigDecimal("1000")));
        assertEquals(RUPEE + "12,345.67", Formats.rupees(new BigDecimal("12345.67")));
        assertEquals(RUPEE + "1,00,000.00", Formats.rupees(new BigDecimal("100000")));
        assertEquals(RUPEE + "12,34,567.89", Formats.rupees(new BigDecimal("1234567.89")));
        assertEquals(RUPEE + "1,23,45,678.00", Formats.rupees(new BigDecimal("12345678")));
    }

    @Test
    void roundsToTwoPlacesAndKeepsSign() {
        assertEquals(RUPEE + "10.01", Formats.rupees(new BigDecimal("10.005")));
        assertEquals("-" + RUPEE + "1,500.00", Formats.rupees(new BigDecimal("-1500")));
    }

    @Test
    void plainAmountHasNoCurrencySignAndShowsZero() {
        assertEquals("2,500.00", Formats.plainAmount(new BigDecimal("2500")));
        assertEquals("0.00", Formats.plainAmount(BigDecimal.ZERO));
    }

    @Test
    void formatsDatesInTheGivenZone() {
        Instant instant = Instant.parse("2026-10-03T20:30:00Z");
        assertEquals("04 Oct 2026, 02:00", Formats.dateTime(instant, ZoneId.of("Asia/Kolkata")));
        assertEquals("04 Oct 2026", Formats.date(instant, ZoneId.of("Asia/Kolkata")));
    }
}
