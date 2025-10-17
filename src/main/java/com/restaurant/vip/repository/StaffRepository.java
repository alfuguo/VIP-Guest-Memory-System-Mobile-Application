package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    
    /**
     * Find staff member by email address
     */
    Optional<Staff> findByEmail(String email);
    
    /**
     * Find staff member by email and active status
     */
    Optional<Staff> findByEmailAndActive(String email, Boolean active);
    
    /**
     * Find all active staff members
     */
    List<Staff> findByActiveTrue();
    
    /**
     * Find staff members by role
     */
    List<Staff> findByRole(StaffRole role);
    
    /**
     * Find active staff members by role
     */
    List<Staff> findByRoleAndActiveTrue(StaffRole role);
    
    /**
     * Check if email exists (for duplicate validation)
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if email exists for different staff member (for update validation)
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Staff s WHERE s.email = :email AND s.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    /**
     * Find staff members with failed login attempts
     */
    @Query("SELECT s FROM Staff s WHERE s.failedLoginAttempts >= :threshold AND s.active = true")
    List<Staff> findByFailedLoginAttemptsGreaterThanEqual(@Param("threshold") Integer threshold);
    
    /**
     * Find locked staff accounts
     */
    @Query("SELECT s FROM Staff s WHERE s.accountLockedUntil IS NOT NULL AND s.accountLockedUntil > :now")
    List<Staff> findLockedAccounts(@Param("now") LocalDateTime now);
    
    /**
     * Find accounts that should be unlocked
     */
    @Query("SELECT s FROM Staff s WHERE s.accountLockedUntil IS NOT NULL AND s.accountLockedUntil <= :now")
    List<Staff> findAccountsToUnlock(@Param("now") LocalDateTime now);
    
    /**
     * Reset failed login attempts
     */
    @Modifying
    @Query("UPDATE Staff s SET s.failedLoginAttempts = 0, s.accountLockedUntil = NULL WHERE s.id = :id")
    void resetFailedLoginAttempts(@Param("id") Long id);
    
    /**
     * Increment failed login attempts
     */
    @Modifying
    @Query("UPDATE Staff s SET s.failedLoginAttempts = s.failedLoginAttempts + 1 WHERE s.id = :id")
    void incrementFailedLoginAttempts(@Param("id") Long id);
    
    /**
     * Lock account until specified time
     */
    @Modifying
    @Query("UPDATE Staff s SET s.accountLockedUntil = :lockUntil WHERE s.id = :id")
    void lockAccount(@Param("id") Long id, @Param("lockUntil") LocalDateTime lockUntil);
    
    /**
     * Unlock accounts that have passed their lock time
     */
    @Modifying
    @Query("UPDATE Staff s SET s.accountLockedUntil = NULL WHERE s.accountLockedUntil <= :now")
    void unlockExpiredAccounts(@Param("now") LocalDateTime now);
    
    /**
     * Deactivate staff member
     */
    @Modifying
    @Query("UPDATE Staff s SET s.active = false WHERE s.id = :id")
    void deactivateStaff(@Param("id") Long id);
    
    /**
     * Search staff by name
     */
    @Query("SELECT s FROM Staff s WHERE " +
           "LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Staff> searchByName(@Param("searchTerm") String searchTerm);
}