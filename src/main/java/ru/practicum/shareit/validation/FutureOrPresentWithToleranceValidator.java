package ru.practicum.shareit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class FutureOrPresentWithToleranceValidator implements ConstraintValidator<FutureOrPresentWithTolerance, OffsetDateTime> {

    private long toleranceMillis;

    @Override
    public void initialize(FutureOrPresentWithTolerance constraintAnnotation) {
        this.toleranceMillis = constraintAnnotation.toleranceMillis();
    }

    @Override
    public boolean isValid(OffsetDateTime time, ConstraintValidatorContext constraintValidatorContext) {
        if (time == null) return true;
        return OffsetDateTime.now().minus(toleranceMillis, ChronoUnit.MILLIS).isBefore(time);
    }

}