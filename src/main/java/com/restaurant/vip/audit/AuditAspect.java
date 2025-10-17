package com.restaurant.vip.audit;

import com.restaurant.vip.entity.AuditLog;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.AuditLogRepository;
import com.restaurant.vip.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic auditing of annotated methods
 */
@Aspect
@Component
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Before("@annotation(auditable)")
    public void auditBefore(JoinPoint joinPoint, Auditable auditable) {
        try {
            setupAuditContext();
        } catch (Exception e) {
            logger.error("Error setting up audit context", e);
        }
    }
    
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            createAuditLog(joinPoint, auditable, result, null);
        } catch (Exception e) {
            logger.error("Error creating audit log for successful operation", e);
        } finally {
            AuditContext.clearContext();
        }
    }
    
    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        try {
            createAuditLog(joinPoint, auditable, null, exception);
        } catch (Exception e) {
            logger.error("Error creating audit log for failed operation", e);
        } finally {
            AuditContext.clearContext();
        }
    }
    
    private void setupAuditContext() {
        AuditContext context = AuditContext.getCurrentContext();
        
        // Get current user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Staff staff = authenticationService.findStaffByEmail(email);
            if (staff != null) {
                context.setStaffId(staff.getId());
                context.setStaffEmail(staff.getEmail());
                context.setStaffRole(staff.getRole().name());
            }
        }
        
        // Get request information
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Get IP address
            String ipAddress = getClientIpAddress(request);
            try {
                context.setIpAddress(InetAddress.getByName(ipAddress));
            } catch (UnknownHostException e) {
                logger.warn("Could not parse IP address: {}", ipAddress);
            }
            
            // Get user agent
            context.setUserAgent(request.getHeader("User-Agent"));
            
            // Get session ID
            if (request.getSession(false) != null) {
                context.setSessionId(request.getSession().getId());
            }
        }
    }
    
    private void createAuditLog(JoinPoint joinPoint, Auditable auditable, Object result, Exception exception) {
        AuditContext context = AuditContext.getCurrentContext();
        
        // Create audit log entry
        AuditLog auditLog = new AuditLog();
        
        // Set staff information
        if (context.getStaffId() != null) {
            Staff staff = new Staff();
            staff.setId(context.getStaffId());
            auditLog.setStaff(staff);
        }
        
        // Set basic audit information
        auditLog.setAction(exception != null ? "FAILED_" + auditable.action().getValue() : auditable.action().getValue());
        auditLog.setTableName(auditable.tableName());
        auditLog.setIpAddress(context.getIpAddress());
        auditLog.setUserAgent(context.getUserAgent());
        
        // Extract record ID from method parameters or result
        Long recordId = extractRecordId(joinPoint, result);
        auditLog.setRecordId(recordId);
        
        // Create audit details
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("method", joinPoint.getSignature().getName());
        auditDetails.put("class", joinPoint.getTarget().getClass().getSimpleName());
        auditDetails.put("description", auditable.description());
        auditDetails.put("staffEmail", context.getStaffEmail());
        auditDetails.put("staffRole", context.getStaffRole());
        auditDetails.put("sessionId", context.getSessionId());
        
        if (exception != null) {
            auditDetails.put("error", exception.getMessage());
            auditDetails.put("errorType", exception.getClass().getSimpleName());
        }
        
        // Log parameters if requested
        if (auditable.logParameters()) {
            Object[] args = joinPoint.getArgs();
            Map<String, Object> parameters = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    // Don't log sensitive information like passwords
                    String paramName = "param" + i;
                    if (args[i].toString().toLowerCase().contains("password")) {
                        parameters.put(paramName, "[REDACTED]");
                    } else {
                        parameters.put(paramName, args[i].toString());
                    }
                }
            }
            auditDetails.put("parameters", parameters);
        }
        
        // Log return value if requested and operation was successful
        if (auditable.logReturnValue() && result != null && exception == null) {
            auditDetails.put("returnValue", result.toString());
        }
        
        auditLog.setNewValues(auditDetails);
        
        // Save audit log
        try {
            auditLogRepository.save(auditLog);
            
            // Log sensitive operations with higher priority
            if (auditable.sensitive()) {
                logger.warn("SENSITIVE OPERATION: {} by {} ({})", 
                    auditable.action(), context.getStaffEmail(), context.getIpAddress());
            }
        } catch (Exception e) {
            logger.error("Failed to save audit log", e);
        }
    }
    
    private Long extractRecordId(JoinPoint joinPoint, Object result) {
        // Try to extract ID from method parameters
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            // Check if it's an entity with getId method
            try {
                if (arg != null && arg.getClass().getMethod("getId") != null) {
                    Object id = arg.getClass().getMethod("getId").invoke(arg);
                    if (id instanceof Long) {
                        return (Long) id;
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors
            }
        }
        
        // Try to extract ID from result
        if (result != null) {
            try {
                if (result instanceof Long) {
                    return (Long) result;
                }
                if (result.getClass().getMethod("getId") != null) {
                    Object id = result.getClass().getMethod("getId").invoke(result);
                    if (id instanceof Long) {
                        return (Long) id;
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors
            }
        }
        
        return null;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}