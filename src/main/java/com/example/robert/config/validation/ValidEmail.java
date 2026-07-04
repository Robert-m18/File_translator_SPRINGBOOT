package com.example.robert.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation do walidacji emaila
 * Przykład: @ValidEmail(message = "Email jest nieprawidłowy")
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEmailValidator.class)
@Documented
public @interface ValidEmail {
    String message() default "Email powinien być w prawidłowym formacie";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

