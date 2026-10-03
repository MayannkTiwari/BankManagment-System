package dev.mayanktiwari.bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The annotated date of birth must be at least {@link #value()} years ago. Null is left to @NotNull. */
@Documented
@Constraint(validatedBy = MinimumAgeValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAge {

    int value() default 18;

    String message() default "You must be at least 18 years old to open an account";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
