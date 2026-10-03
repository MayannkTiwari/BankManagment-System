package dev.mayanktiwari.bank.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MinimumAgeValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 3);

    private MinimumAgeValidator validator() {
        MinimumAgeValidator validator = new MinimumAgeValidator();
        validator.setMinimumYears(18);
        return validator;
    }

    @Test
    void acceptsExactlyEighteenOnTheBirthday() {
        assertTrue(validator().isValid(LocalDate.of(2008, 10, 3), TODAY));
    }

    @Test
    void rejectsOneDayShortOfEighteen() {
        assertFalse(validator().isValid(LocalDate.of(2008, 10, 4), TODAY));
    }

    @Test
    void rejectsFutureDates() {
        assertFalse(validator().isValid(LocalDate.of(2027, 1, 1), TODAY));
    }

    @Test
    void handlesLeapDayBirthdays() {
        // Born 29 Feb 2008 turns 18 on 28 Feb 2026 under java.time rules
        assertTrue(validator().isValid(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 3, 1)));
        assertFalse(validator().isValid(LocalDate.of(2008, 2, 29), LocalDate.of(2026, 2, 27)));
    }

    @Test
    void leavesNullToNotNull() {
        assertTrue(validator().isValid(null, TODAY));
    }
}
