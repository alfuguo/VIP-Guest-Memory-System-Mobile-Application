package com.restaurant.vip.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Validator for SafeHtml annotation that sanitizes HTML content to prevent XSS attacks
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {
    
    private PolicyFactory policy;
    private int maxLength;
    
    @Override
    public void initialize(SafeHtml constraintAnnotation) {
        this.maxLength = constraintAnnotation.maxLength();
        
        HtmlPolicyBuilder builder = new HtmlPolicyBuilder();
        
        if (constraintAnnotation.allowBasicFormatting()) {
            // Allow basic formatting tags for rich text fields
            builder.allowElements("b", "i", "u", "em", "strong", "br", "p")
                   .allowAttributes("class").onElements("p");
        }
        // For most fields, we don't allow any HTML tags
        
        this.policy = builder.toFactory();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }
        
        // Sanitize the HTML content
        String sanitized = policy.sanitize(value);
        
        // Check if sanitization removed content (indicating potential XSS)
        if (!sanitized.equals(value.trim())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Content contains potentially unsafe HTML elements or scripts")
                .addConstraintViolation();
            return false;
        }
        
        // Check length after sanitization
        if (sanitized.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Content exceeds maximum length of " + maxLength + " characters")
                .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}