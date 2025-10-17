package com.restaurant.vip.controller;

import com.restaurant.vip.audit.Auditable;
import com.restaurant.vip.audit.AuditAction;
import com.restaurant.vip.dto.PagedResponse;
import com.restaurant.vip.entity.AuditLog;
import com.restaurant.vip.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for audit log management (Manager access only)
 */
@RestController
@RequestMapping("/admin/audit")
@PreAuthorize("hasRole('MANAGER')")
public class AuditController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * Get all audit logs with pagination
     */
    @GetMapping
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View audit logs")
    public ResponseEntity<PagedResponse<AuditLog>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get audit logs by staff member
     */
    @GetMapping("/staff/{staffId}")
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View staff audit logs")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogsByStaff(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByStaff(staffId, pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get audit logs by action type
     */
    @GetMapping("/action/{action}")
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View audit logs by action")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByAction(action, pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get audit logs by table name
     */
    @GetMapping("/table/{tableName}")
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View audit logs by table")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogsByTable(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByTableName(tableName, pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get audit logs by date range
     */
    @GetMapping("/date-range")
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View audit logs by date range")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByDateRange(startDate, endDate, pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get security violations
     */
    @GetMapping("/security-violations")
    @Auditable(action = AuditAction.READ, tableName = "audit_log", description = "View security violations", sensitive = true)
    public ResponseEntity<PagedResponse<AuditLog>> getSecurityViolations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogService.getSecurityViolations(pageable);
        
        PagedResponse<AuditLog> response = new PagedResponse<>(
            auditLogs.getContent(),
            auditLogs.getNumber(),
            auditLogs.getSize(),
            auditLogs.getTotalElements(),
            auditLogs.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }
}