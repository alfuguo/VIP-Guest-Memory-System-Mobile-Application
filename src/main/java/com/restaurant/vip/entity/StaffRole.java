package com.restaurant.vip.entity;

public enum StaffRole {
    HOST("Host"),
    SERVER("Server"),
    MANAGER("Manager");
    
    private final String displayName;
    
    StaffRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean hasManagerPrivileges() {
        return this == MANAGER;
    }
    
    public boolean canEditAllGuests() {
        return this == MANAGER;
    }
    
    public boolean canEditAllVisits() {
        return this == MANAGER;
    }
    
    public boolean canViewAuditLogs() {
        return this == MANAGER;
    }
}