package com.restaurant.vip.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation to ensure HTML content is safe and sanitized
 */
@Documented
@Constraint(validatedBy = SafeHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeHtml {
    
    String message() default "HTML content contains potentially unsafe elements";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Whether to allow basic formatting tags like <b>, <i>, <u>
     */
    boolean allowBasicFormatting() default false;
    
    /**
     * Maximum length of the sanitized content
     */
    int maxLength() default 1000;
}