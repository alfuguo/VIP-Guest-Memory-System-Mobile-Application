package com.restaurant.vip.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input to prevent XSS and injection attacks
 */
public class InputSanitizer {
    
    // HTML sanitization policies
    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();
    
    private static final PolicyFactory BASIC_FORMATTING_POLICY = new HtmlPolicyBuilder()
        .allowElements("b", "i", "u", "em", "strong", "br", "p")
        .allowAttributes("class").onElements("p")
        .toFactory();
    
    // Patterns for additional sanitization
    private static final Pattern SCRIPT_PATTERN = 
        Pattern.compile("(?i)<script[^>]*>.*?</script>", Pattern.DOTALL);
    
    private static final Pattern JAVASCRIPT_PATTERN = 
        Pattern.compile("(?i)javascript:", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ON_EVENT_PATTERN = 
        Pattern.compile("(?i)\\s*on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    
    /**
     * Sanitize text input by removing all HTML tags and potentially dangerous content
     */
    public static String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove HTML tags completely
        String sanitized = STRICT_POLICY.sanitize(input);
        
        // Additional cleaning
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Normalize whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        return sanitized;
    }
    
    /**
     * Sanitize HTML content allowing basic formatting tags
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        // Allow basic formatting but remove dangerous content
        String sanitized = BASIC_FORMATTING_POLICY.sanitize(input);
        
        // Additional security checks
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        
        return sanitized;
    }
    
    /**
     * Sanitize search query input
     */
    public static String sanitizeSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        
        String sanitized = sanitizeText(query);
        
        // Remove SQL injection patterns
        sanitized = sanitized.replaceAll("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)", "");
        sanitized = sanitized.replaceAll("[';\"\\-\\-]", "");
        
        // Limit length
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize phone number input
     */
    public static String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        
        // Remove all non-digit and non-plus characters
        String sanitized = phone.replaceAll("[^\\d+]", "");
        
        // Ensure only one plus sign at the beginning
        if (sanitized.startsWith("+")) {
            sanitized = "+" + sanitized.substring(1).replaceAll("\\+", "");
        } else {
            sanitized = sanitized.replaceAll("\\+", "");
        }
        
        return sanitized;
    }
    
    /**
     * Sanitize email input
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        String sanitized = sanitizeText(email);
        
        // Additional email-specific cleaning
        sanitized = sanitized.toLowerCase().trim();
        
        return sanitized;
    }
    
    /**
     * Check if input contains potentially dangerous patterns
     */
    public static boolean containsDangerousPatterns(String input) {
        if (input == null) {
            return false;
        }
        
        String lowerInput = input.toLowerCase();
        
        // Check for common attack patterns
        return lowerInput.contains("<script") ||
               lowerInput.contains("javascript:") ||
               lowerInput.contains("vbscript:") ||
               lowerInput.contains("onload=") ||
               lowerInput.contains("onerror=") ||
               lowerInput.contains("onclick=") ||
               lowerInput.contains("union select") ||
               lowerInput.contains("drop table") ||
               lowerInput.contains("'; drop") ||
               lowerInput.contains("1=1") ||
               lowerInput.contains("1' or '1'='1");
    }
}