package com.restaurant.vip.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to prevent SQL injection patterns in input
 */
@Documented
@Constraint(validatedBy = NoSqlInjectionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSqlInjection {
    
    String message() default "Input contains potentially dangerous SQL patterns";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}