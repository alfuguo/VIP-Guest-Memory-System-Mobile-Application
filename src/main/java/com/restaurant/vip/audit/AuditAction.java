package com.restaurant.vip.audit;

/**
 * Enumeration of audit actions
 */
public enum AuditAction {
    CREATE("CREATE"),
    READ("READ"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    ACCESS_DENIED("ACCESS_DENIED"),
    SECURITY_VIOLATION("SECURITY_VIOLATION"),
    EXPORT("EXPORT"),
    IMPORT("IMPORT");
    
    private final String value;
    
    AuditAction(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}