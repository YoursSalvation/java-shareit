package ru.practicum.shareit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FutureOrPresentWithToleranceValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureOrPresentWithTolerance {

    String message() default "Should be in the present or future, with allowed tolerance";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long toleranceMillis() default 2000;

}