package com.restaurant.vip.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator to detect and prevent SQL injection patterns in input strings
 */
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {
    
    // Common SQL injection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i).*('|(\\-\\-)|(;)|(\\|)|(\\*)).*"),
        Pattern.compile("(?i).*(union|select|insert|update|delete|drop|create|alter|exec|execute).*"),
        Pattern.compile("(?i).*(script|javascript|vbscript|onload|onerror|onclick).*"),
        Pattern.compile("(?i).*(<|>|&lt;|&gt;).*"),
        Pattern.compile("(?i).*(\\bor\\b|\\band\\b)\\s*\\d+\\s*=\\s*\\d+.*"),
        Pattern.compile("(?i).*\\d+\\s*(=|!=|<>)\\s*\\d+.*"),
        Pattern.compile("(?i).*(char|ascii|substring|length|user|database|version)\\s*\\(.*")
    };
    
    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let other validators handle null/empty
        }
        
        String trimmedValue = value.trim();
        
        // Check against SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(trimmedValue).matches()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Input contains potentially dangerous patterns that could be used for SQL injection")
                    .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}