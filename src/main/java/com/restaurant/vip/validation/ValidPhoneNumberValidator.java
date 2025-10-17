package com.restaurant.vip.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Enhanced phone number validator with better security and format checking
 */
public class ValidPhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    // More comprehensive phone number patterns
    private static final Pattern INTERNATIONAL_PATTERN = 
        Pattern.compile("^\\+[1-9]\\d{1,14}$");
    
    private static final Pattern US_PATTERN = 
        Pattern.compile("^\\+?1?[2-9]\\d{2}[2-9]\\d{2}\\d{4}$");
    
    private static final Pattern GENERAL_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{7,14}$");
    
    // Patterns to reject (security)
    private static final Pattern[] INVALID_PATTERNS = {
        Pattern.compile(".*[<>\"'&].*"), // HTML/XSS characters
        Pattern.compile(".*[;|*].*"),    // SQL injection characters
        Pattern.compile(".*script.*", Pattern.CASE_INSENSITIVE), // Script tags
        Pattern.compile("^[0+]+$"),      // All zeros or plus signs
        Pattern.compile(".*\\s.*")       // No spaces allowed
    };
    
    private boolean allowInternational;
    
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.allowInternational = constraintAnnotation.allowInternational();
    }
    
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Let @NotNull/@NotBlank handle null/empty
        }
        
        String cleanPhone = phoneNumber.trim();
        
        // Check for invalid patterns first (security)
        for (Pattern invalidPattern : INVALID_PATTERNS) {
            if (invalidPattern.matcher(cleanPhone).matches()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Phone number contains invalid characters")
                    .addConstraintViolation();
                return false;
            }
        }
        
        // Length check
        if (cleanPhone.length() < 8 || cleanPhone.length() > 17) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Phone number must be between 8 and 17 characters")
                .addConstraintViolation();
            return false;
        }
        
        // Format validation
        if (allowInternational) {
            return INTERNATIONAL_PATTERN.matcher(cleanPhone).matches() ||
                   US_PATTERN.matcher(cleanPhone).matches() ||
                   GENERAL_PATTERN.matcher(cleanPhone).matches();
        } else {
            return US_PATTERN.matcher(cleanPhone).matches();
        }
    }
}