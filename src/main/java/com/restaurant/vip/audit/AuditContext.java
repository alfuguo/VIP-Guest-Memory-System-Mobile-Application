package com.restaurant.vip.audit;

import java.net.InetAddress;

/**
 * Context holder for audit information during request processing
 */
public class AuditContext {
    
    private static final ThreadLocal<AuditContext> contextHolder = new ThreadLocal<>();
    
    private Long staffId;
    private String staffEmail;
    private String staffRole;
    private InetAddress ipAddress;
    private String userAgent;
    private String sessionId;
    
    public static AuditContext getCurrentContext() {
        AuditContext context = contextHolder.get();
        if (context == null) {
            context = new AuditContext();
            contextHolder.set(context);
        }
        return context;
    }
    
    public static void setCurrentContext(AuditContext context) {
        contextHolder.set(context);
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
    
    // Getters and Setters
    public Long getStaffId() {
        return staffId;
    }
    
    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
    
    public String getStaffEmail() {
        return staffEmail;
    }
    
    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }
    
    public String getStaffRole() {
        return staffRole;
    }
    
    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }
    
    public InetAddress getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}