package dev.mayanktiwari.bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.ZoneId;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

    private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    private int minimumYears;

    @Override
    public void initialize(MinimumAge annotation) {
        this.minimumYears = annotation.value();
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        return isValid(dateOfBirth, LocalDate.now(INDIA));
    }

    boolean isValid(LocalDate dateOfBirth, LocalDate today) {
        if (dateOfBirth == null) {
            return true;
        }
        return !dateOfBirth.plusYears(minimumYears).isAfter(today);
    }

    void setMinimumYears(int minimumYears) {
        this.minimumYears = minimumYears;
    }
}
