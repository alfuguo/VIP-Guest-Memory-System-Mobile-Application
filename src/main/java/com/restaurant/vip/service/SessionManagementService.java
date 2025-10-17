package com.restaurant.vip.service;

import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
public class SessionManagementService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${security.session-timeout}")
    private long sessionTimeout; // 30 minutes in milliseconds

    @Value("${security.lockout-duration}")
    private long lockoutDuration; // 30 minutes in milliseconds

    // In-memory session tracking (in production, use Redis or database)
    private final ConcurrentMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    public static class SessionInfo {
        private final Long staffId;
        private final String email;
        private LocalDateTime lastActivity;
        private final LocalDateTime loginTime;

        public SessionInfo(Long staffId, String email) {
            this.staffId = staffId;
            this.email = email;
            this.lastActivity = LocalDateTime.now();
            this.loginTime = LocalDateTime.now();
        }

        public Long getStaffId() { return staffId; }
        public String getEmail() { return email; }
        public LocalDateTime getLastActivity() { return lastActivity; }
        public LocalDateTime getLoginTime() { return loginTime; }
        
        public void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }
        
        public boolean isExpired(long timeoutMillis) {
            return lastActivity.isBefore(LocalDateTime.now().minusNanos(timeoutMillis * 1_000_000));
        }
    }

    /**
     * Create a new session for a staff member
     */
    public void createSession(String token, Staff staff) {
        SessionInfo sessionInfo = new SessionInfo(staff.getId(), staff.getEmail());
        activeSessions.put(token, sessionInfo);
        
        auditLogService.logDataAccess(staff, "session", staff.getId(), "SESSION_CREATED");
    }

    /**
     * Update session activity timestamp
     */
    public void updateSessionActivity(String token) {
        SessionInfo session = activeSessions.get(token);
        if (session != null) {
            session.updateActivity();
        }
    }

    /**
     * Remove a session (logout)
     */
    public void removeSession(String token) {
        SessionInfo session = activeSessions.remove(token);
        if (session != null) {
            Staff staff = staffRepository.findById(session.getStaffId()).orElse(null);
            if (staff != null) {
                auditLogService.logDataAccess(staff, "session", staff.getId(), "SESSION_REMOVED");
            }
        }
    }

    /**
     * Check if a session is valid and not expired
     */
    public boolean isSessionValid(String token) {
        SessionInfo session = activeSessions.get(token);
        if (session == null) {
            return false;
        }

        if (session.isExpired(sessionTimeout)) {
            // Session expired, remove it
            removeExpiredSession(token, session);
            return false;
        }

        // Update activity timestamp
        session.updateActivity();
        return true;
    }

    /**
     * Get session information
     */
    public SessionInfo getSessionInfo(String token) {
        return activeSessions.get(token);
    }

    /**
     * Get all active sessions for a staff member
     */
    public long getActiveSessionCount(Long staffId) {
        return activeSessions.values().stream()
                .filter(session -> session.getStaffId().equals(staffId))
                .count();
    }

    /**
     * Remove all sessions for a staff member (force logout)
     */
    public void removeAllSessionsForStaff(Long staffId) {
        activeSessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getStaffId().equals(staffId)) {
                Staff staff = staffRepository.findById(staffId).orElse(null);
                if (staff != null) {
                    auditLogService.logDataAccess(staff, "session", staffId, "SESSION_FORCE_REMOVED");
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Scheduled task to clean up expired sessions
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusNanos(sessionTimeout * 1_000_000);

        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo session = entry.getValue();
            if (session.getLastActivity().isBefore(cutoff)) {
                // Log session expiration
                Staff staff = staffRepository.findById(session.getStaffId()).orElse(null);
                if (staff != null) {
                    auditLogService.logDataAccess(staff, "session", staff.getId(), "SESSION_EXPIRED");
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Scheduled task to unlock accounts that have passed their lockout time
     * Runs every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void unlockExpiredAccounts() {
        LocalDateTime now = LocalDateTime.now();
        List<Staff> accountsToUnlock = staffRepository.findAccountsToUnlock(now);
        
        for (Staff staff : accountsToUnlock) {
            staff.setAccountLockedUntil(null);
            staff.setFailedLoginAttempts(0);
            staffRepository.save(staff);
            
            auditLogService.logDataAccess(staff, "staff", staff.getId(), "ACCOUNT_UNLOCKED");
        }
    }

    private void removeExpiredSession(String token, SessionInfo session) {
        activeSessions.remove(token);
        Staff staff = staffRepository.findById(session.getStaffId()).orElse(null);
        if (staff != null) {
            auditLogService.logDataAccess(staff, "session", staff.getId(), "SESSION_EXPIRED");
        }
    }

    /**
     * Handle failed login attempt and potential account lockout
     */
    public void handleFailedLoginAttempt(String email) {
        Staff staff = staffRepository.findByEmail(email).orElse(null);
        if (staff == null) {
            return;
        }

        int failedAttempts = staff.getFailedLoginAttempts() + 1;
        staff.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= 5) { // Max failed attempts from config
            LocalDateTime lockUntil = LocalDateTime.now().plusNanos(lockoutDuration * 1_000_000);
            staff.setAccountLockedUntil(lockUntil);
            
            // Remove all active sessions for this staff member
            removeAllSessionsForStaff(staff.getId());
            
            auditLogService.logAccountLocked(staff);
        }

        staffRepository.save(staff);
    }

    /**
     * Reset failed login attempts on successful login
     */
    public void resetFailedLoginAttempts(Staff staff) {
        if (staff.getFailedLoginAttempts() > 0) {
            staff.setFailedLoginAttempts(0);
            staff.setAccountLockedUntil(null);
            staffRepository.save(staff);
        }
    }

    /**
     * Get session statistics
     */
    public SessionStatistics getSessionStatistics() {
        int totalActiveSessions = activeSessions.size();
        long uniqueUsers = activeSessions.values().stream()
                .map(SessionInfo::getStaffId)
                .distinct()
                .count();

        return new SessionStatistics(totalActiveSessions, uniqueUsers);
    }

    public static class SessionStatistics {
        private final int totalActiveSessions;
        private final long uniqueActiveUsers;

        public SessionStatistics(int totalActiveSessions, long uniqueActiveUsers) {
            this.totalActiveSessions = totalActiveSessions;
            this.uniqueActiveUsers = uniqueActiveUsers;
        }

        public int getTotalActiveSessions() { return totalActiveSessions; }
        public long getUniqueActiveUsers() { return uniqueActiveUsers; }
    }
}