package com.restaurant.vip.repository;

import com.restaurant.vip.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by staff member
     */
    List<AuditLog> findByStaffIdOrderByCreatedAtDesc(Long staffId);
    
    /**
     * Find audit logs by staff member with pagination
     */
    Page<AuditLog> findByStaffIdOrderByCreatedAtDesc(Long staffId, Pageable pageable);
    
    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    
    /**
     * Find audit logs by action type with pagination
     */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    /**
     * Find audit logs by table name
     */
    List<AuditLog> findByTableNameOrderByCreatedAtDesc(String tableName);
    
    /**
     * Find audit logs by table name with pagination
     */
    Page<AuditLog> findByTableNameOrderByCreatedAtDesc(String tableName, Pageable pageable);
    
    /**
     * Find audit logs for specific record
     */
    List<AuditLog> findByTableNameAndRecordIdOrderByCreatedAtDesc(String tableName, Long recordId);
    
    /**
     * Find audit logs in date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find audit logs in date range with pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find all audit logs with pagination
     */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find audit logs containing specific action pattern
     */
    Page<AuditLog> findByActionContainingOrderByCreatedAtDesc(String actionPattern, Pageable pageable);
    
    /**
     * Find recent audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    /**
     * Find audit logs by action and table
     */
    List<AuditLog> findByActionAndTableNameOrderByCreatedAtDesc(String action, String tableName);
    
    /**
     * Find audit logs by staff and action
     */
    List<AuditLog> findByStaffIdAndActionOrderByCreatedAtDesc(Long staffId, String action);
    
    /**
     * Find audit logs by staff and table
     */
    List<AuditLog> findByStaffIdAndTableNameOrderByCreatedAtDesc(Long staffId, String tableName);
    
    /**
     * Count audit logs by staff member
     */
    long countByStaffId(Long staffId);
    
    /**
     * Count audit logs by action
     */
    long countByAction(String action);
    
    /**
     * Count audit logs by table name
     */
    long countByTableName(String tableName);
    
    /**
     * Count audit logs in date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find audit logs for data access tracking
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'READ' AND a.tableName = :tableName ORDER BY a.createdAt DESC")
    List<AuditLog> findDataAccessLogs(@Param("tableName") String tableName);
    
    /**
     * Find audit logs for data modifications
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('CREATE', 'UPDATE', 'DELETE') AND a.tableName = :tableName ORDER BY a.createdAt DESC")
    List<AuditLog> findDataModificationLogs(@Param("tableName") String tableName);
    
    /**
     * Find suspicious activity (multiple failed actions by same staff)
     */
    @Query("SELECT a.staff.id, COUNT(a) as failureCount FROM AuditLog a " +
           "WHERE a.action LIKE '%FAILED%' AND a.createdAt >= :since " +
           "GROUP BY a.staff.id " +
           "HAVING COUNT(a) >= :threshold " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> findSuspiciousActivity(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);
    
    /**
     * Get audit statistics for a staff member
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN a.action = 'CREATE' THEN 1 END) as creates, " +
           "COUNT(CASE WHEN a.action = 'UPDATE' THEN 1 END) as updates, " +
           "COUNT(CASE WHEN a.action = 'DELETE' THEN 1 END) as deletes, " +
           "COUNT(CASE WHEN a.action = 'READ' THEN 1 END) as reads " +
           "FROM AuditLog a WHERE a.staff.id = :staffId AND a.createdAt >= :since")
    Object[] getStaffAuditStatistics(@Param("staffId") Long staffId, @Param("since") LocalDateTime since);
    
    /**
     * Advanced audit log search
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:staffId IS NULL OR a.staff.id = :staffId) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:tableName IS NULL OR a.tableName = :tableName) " +
           "AND (:recordId IS NULL OR a.recordId = :recordId) " +
           "AND (:startDate IS NULL OR a.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> advancedSearch(
        @Param("staffId") Long staffId,
        @Param("action") String action,
        @Param("tableName") String tableName,
        @Param("recordId") Long recordId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Clean up old audit logs (for maintenance)
     */
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}