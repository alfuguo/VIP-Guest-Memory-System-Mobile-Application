package com.restaurant.vip.config;

import com.restaurant.vip.util.InputSanitizer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter to sanitize all incoming HTTP request parameters
 */
@Component
@Order(1)
public class RequestSanitizationFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            SanitizedHttpServletRequestWrapper wrappedRequest = 
                new SanitizedHttpServletRequestWrapper(httpRequest);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
    
    /**
     * Wrapper class to sanitize request parameters
     */
    private static class SanitizedHttpServletRequestWrapper extends HttpServletRequestWrapper {
        
        private final Map<String, String[]> sanitizedParameters;
        
        public SanitizedHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
            this.sanitizedParameters = sanitizeParameters(request.getParameterMap());
        }
        
        @Override
        public String getParameter(String name) {
            String[] values = getParameterValues(name);
            return values != null && values.length > 0 ? values[0] : null;
        }
        
        @Override
        public String[] getParameterValues(String name) {
            return sanitizedParameters.get(name);
        }
        
        @Override
        public Map<String, String[]> getParameterMap() {
            return sanitizedParameters;
        }
        
        private Map<String, String[]> sanitizeParameters(Map<String, String[]> originalParameters) {
            Map<String, String[]> sanitizedMap = new HashMap<>();
            
            for (Map.Entry<String, String[]> entry : originalParameters.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                
                if (values != null) {
                    String[] sanitizedValues = new String[values.length];
                    for (int i = 0; i < values.length; i++) {
                        // Apply appropriate sanitization based on parameter name
                        if (key.toLowerCase().contains("search") || key.toLowerCase().contains("query")) {
                            sanitizedValues[i] = InputSanitizer.sanitizeSearchQuery(values[i]);
                        } else if (key.toLowerCase().contains("phone")) {
                            sanitizedValues[i] = InputSanitizer.sanitizePhoneNumber(values[i]);
                        } else if (key.toLowerCase().contains("email")) {
                            sanitizedValues[i] = InputSanitizer.sanitizeEmail(values[i]);
                        } else {
                            sanitizedValues[i] = InputSanitizer.sanitizeText(values[i]);
                        }
                    }
                    sanitizedMap.put(key, sanitizedValues);
                }
            }
            
            return sanitizedMap;
        }
    }
}