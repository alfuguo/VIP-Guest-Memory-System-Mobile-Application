package com.restaurant.vip.service;

import com.restaurant.vip.audit.AuditContext;
import com.restaurant.vip.entity.AuditLog;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logSuccessfulLogin(Staff staff) {
        AuditLog auditLog = new AuditLog(staff, "LOGIN_SUCCESS", "staff", staff.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", staff.getEmail());
        details.put("role", staff.getRole().name());
        details.put("message", "User successfully logged in");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logFailedLogin(Staff staff, String reason) {
        AuditLog auditLog = new AuditLog(staff, "LOGIN_FAILED", "staff", staff.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", staff.getEmail());
        details.put("reason", reason);
        details.put("failedAttempts", staff.getFailedLoginAttempts() + 1);
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logAccountLocked(Staff staff) {
        AuditLog auditLog = new AuditLog(staff, "ACCOUNT_LOCKED", "staff", staff.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", staff.getEmail());
        details.put("reason", "Maximum failed login attempts exceeded");
        details.put("lockedUntil", staff.getAccountLockedUntil());
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logTokenRefresh(Staff staff) {
        AuditLog auditLog = new AuditLog(staff, "TOKEN_REFRESH", "staff", staff.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", staff.getEmail());
        details.put("message", "Access token refreshed");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logLogout(Staff staff) {
        AuditLog auditLog = new AuditLog(staff, "LOGOUT", "staff", staff.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", staff.getEmail());
        details.put("message", "User logged out");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logDataAccess(Staff staff, String tableName, Long recordId, String action) {
        AuditLog auditLog = new AuditLog(staff, action, tableName, recordId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("staffEmail", staff.getEmail());
        details.put("staffRole", staff.getRole().name());
        details.put("accessType", action);
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }

    public void logDataModification(Staff staff, String tableName, Long recordId, 
                                  String action, Map<String, Object> oldValues, 
                                  Map<String, Object> newValues) {
        AuditLog auditLog = new AuditLog(staff, action, tableName, recordId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        
        auditLogRepository.save(auditLog);
    }

    // Visit-related audit methods
    
    public void logVisitCreated(Long staffId, Long visitId, Long guestId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        
        AuditLog auditLog = new AuditLog(staff, "VISIT_CREATED", "visits", visitId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("visitId", visitId);
        details.put("guestId", guestId);
        details.put("message", "New visit record created");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }
    
    public void logVisitUpdated(Long staffId, Long visitId, Long guestId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        
        AuditLog auditLog = new AuditLog(staff, "VISIT_UPDATED", "visits", visitId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("visitId", visitId);
        details.put("guestId", guestId);
        details.put("message", "Visit record updated");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }
    
    public void logVisitDeleted(Long staffId, Long visitId, Long guestId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        
        AuditLog auditLog = new AuditLog(staff, "VISIT_DELETED", "visits", visitId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("visitId", visitId);
        details.put("guestId", guestId);
        details.put("message", "Visit record deleted");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }
    
    public void logVisitNotesUpdated(Long staffId, Long visitId, Long guestId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        
        AuditLog auditLog = new AuditLog(staff, "VISIT_NOTES_UPDATED", "visits", visitId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("visitId", visitId);
        details.put("guestId", guestId);
        details.put("message", "Visit notes updated");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }
    
    public void logVisitAccessed(Long staffId, Long visitId, Long guestId) {
        Staff staff = new Staff();
        staff.setId(staffId);
        
        AuditLog auditLog = new AuditLog(staff, "VISIT_ACCESSED", "visits", visitId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("visitId", visitId);
        details.put("guestId", guestId);
        details.put("message", "Visit record accessed");
        auditLog.setNewValues(details);
        
        auditLogRepository.save(auditLog);
    }
    
    // Enhanced guest audit logging methods
    
    public void logGuestCreated(Staff staff, Guest guest) {
        AuditLog auditLog = createBaseAuditLog(staff, "GUEST_CREATED", "guests", guest.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("guestName", guest.getFirstName() + " " + (guest.getLastName() != null ? guest.getLastName() : ""));
        details.put("phone", maskPhoneNumber(guest.getPhone()));
        details.put("email", maskEmail(guest.getEmail()));
        details.put("message", "New guest profile created");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
    }
    
    public void logGuestUpdated(Staff staff, Guest oldGuest, Guest newGuest) {
        AuditLog auditLog = createBaseAuditLog(staff, "GUEST_UPDATED", "guests", newGuest.getId());
        
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        // Track changes in sensitive fields
        if (!oldGuest.getFirstName().equals(newGuest.getFirstName())) {
            oldValues.put("firstName", oldGuest.getFirstName());
            newValues.put("firstName", newGuest.getFirstName());
        }
        
        if (!oldGuest.getPhone().equals(newGuest.getPhone())) {
            oldValues.put("phone", maskPhoneNumber(oldGuest.getPhone()));
            newValues.put("phone", maskPhoneNumber(newGuest.getPhone()));
        }
        
        if (oldGuest.getEmail() != null && newGuest.getEmail() != null && 
            !oldGuest.getEmail().equals(newGuest.getEmail())) {
            oldValues.put("email", maskEmail(oldGuest.getEmail()));
            newValues.put("email", maskEmail(newGuest.getEmail()));
        }
        
        newValues.put("message", "Guest profile updated");
        newValues.put("guestName", newGuest.getFirstName() + " " + (newGuest.getLastName() != null ? newGuest.getLastName() : ""));
        
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        
        saveAuditLog(auditLog);
    }
    
    public void logGuestDeleted(Staff staff, Guest guest) {
        AuditLog auditLog = createBaseAuditLog(staff, "GUEST_DELETED", "guests", guest.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("guestName", guest.getFirstName() + " " + (guest.getLastName() != null ? guest.getLastName() : ""));
        details.put("phone", maskPhoneNumber(guest.getPhone()));
        details.put("message", "Guest profile deleted");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
    }
    
    public void logGuestAccessed(Staff staff, Guest guest) {
        AuditLog auditLog = createBaseAuditLog(staff, "GUEST_ACCESSED", "guests", guest.getId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("guestName", guest.getFirstName() + " " + (guest.getLastName() != null ? guest.getLastName() : ""));
        details.put("phone", maskPhoneNumber(guest.getPhone()));
        details.put("message", "Guest profile accessed");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
    }
    
    public void logGuestSearch(Staff staff, String searchQuery, int resultCount) {
        AuditLog auditLog = createBaseAuditLog(staff, "GUEST_SEARCH", "guests", null);
        
        Map<String, Object> details = new HashMap<>();
        details.put("searchQuery", searchQuery);
        details.put("resultCount", resultCount);
        details.put("message", "Guest search performed");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
    }
    
    // Security-related audit logging
    
    public void logSecurityViolation(Staff staff, String violationType, String details, InetAddress ipAddress) {
        AuditLog auditLog = createBaseAuditLog(staff, "SECURITY_VIOLATION", "security", null);
        auditLog.setIpAddress(ipAddress);
        
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("violationType", violationType);
        auditDetails.put("details", details);
        auditDetails.put("severity", "HIGH");
        auditDetails.put("message", "Security violation detected");
        auditLog.setNewValues(auditDetails);
        
        saveAuditLog(auditLog);
        
        // Also log to security logger
        logger.warn("SECURITY VIOLATION: {} by {} from {}: {}", 
            violationType, staff != null ? staff.getEmail() : "UNKNOWN", ipAddress, details);
    }
    
    public void logUnauthorizedAccess(String email, String resource, InetAddress ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("UNAUTHORIZED_ACCESS");
        auditLog.setTableName("security");
        auditLog.setIpAddress(ipAddress);
        
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("resource", resource);
        details.put("message", "Unauthorized access attempt");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
        
        logger.warn("UNAUTHORIZED ACCESS: {} attempted to access {} from {}", email, resource, ipAddress);
    }
    
    public void logDataExport(Staff staff, String dataType, int recordCount) {
        AuditLog auditLog = createBaseAuditLog(staff, "DATA_EXPORT", dataType, null);
        
        Map<String, Object> details = new HashMap<>();
        details.put("dataType", dataType);
        details.put("recordCount", recordCount);
        details.put("message", "Data export performed");
        auditLog.setNewValues(details);
        
        saveAuditLog(auditLog);
        
        logger.info("DATA EXPORT: {} exported {} records of type {} by {}", 
            recordCount, dataType, staff.getEmail());
    }
    
    // Query methods for audit logs
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByStaff(Long staffId, Pageable pageable) {
        return auditLogRepository.findByStaffIdOrderByCreatedAtDesc(staffId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByTableName(String tableName, Pageable pageable) {
        return auditLogRepository.findByTableNameOrderByCreatedAtDesc(tableName, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<AuditLog> getSecurityViolations(Pageable pageable) {
        return auditLogRepository.findByActionContainingOrderByCreatedAtDesc("SECURITY", pageable);
    }
    
    // Helper methods
    
    private AuditLog createBaseAuditLog(Staff staff, String action, String tableName, Long recordId) {
        AuditLog auditLog = new AuditLog(staff, action, tableName, recordId);
        
        // Set context information if available
        AuditContext context = AuditContext.getCurrentContext();
        if (context != null) {
            auditLog.setIpAddress(context.getIpAddress());
            auditLog.setUserAgent(context.getUserAgent());
        }
        
        return auditLog;
    }
    
    private void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }
    
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return "[MASKED]";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 2);
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "[MASKED]";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return "**@" + parts[1];
        }
        return localPart.substring(0, 2) + "**@" + parts[1];
    }
}