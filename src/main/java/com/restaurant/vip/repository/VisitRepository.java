package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    
    /**
     * Find all visits for a specific guest, ordered by date/time descending
     */
    List<Visit> findByGuestIdOrderByVisitDateDescVisitTimeDesc(Long guestId);
    
    /**
     * Find visits for a guest with pagination
     */
    Page<Visit> findByGuestIdOrderByVisitDateDescVisitTimeDesc(Long guestId, Pageable pageable);
    
    /**
     * Find visits by staff member
     */
    List<Visit> findByStaffIdOrderByVisitDateDescVisitTimeDesc(Long staffId);
    
    /**
     * Find visits on specific date
     */
    List<Visit> findByVisitDateOrderByVisitTimeDesc(LocalDate visitDate);
    
    /**
     * Find visits on specific date ordered by time ascending (for pre-arrival notifications)
     */
    List<Visit> findByVisitDateOrderByVisitTimeAsc(LocalDate visitDate);
    
    /**
     * Find visits in date range
     */
    @Query("SELECT v FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find visits in date range with pagination
     */
    @Query("SELECT v FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate ORDER BY v.visitDate DESC, v.visitTime DESC")
    Page<Visit> findByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
    
    /**
     * Find recent visits (last N days)
     */
    @Query("SELECT v FROM Visit v WHERE v.visitDate >= :cutoffDate ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findRecentVisits(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Find today's visits
     */
    @Query("SELECT v FROM Visit v WHERE v.visitDate = CURRENT_DATE ORDER BY v.visitTime DESC")
    List<Visit> findTodaysVisits();
    
    /**
     * Find visits by table number
     */
    List<Visit> findByTableNumberOrderByVisitDateDescVisitTimeDesc(String tableNumber);
    
    /**
     * Find visits with party size greater than or equal to specified size
     */
    @Query("SELECT v FROM Visit v WHERE v.partySize >= :minPartySize ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByPartySizeGreaterThanEqual(@Param("minPartySize") Integer minPartySize);
    
    /**
     * Get last visit for a guest
     */
    @Query("SELECT v FROM Visit v WHERE v.guest.id = :guestId ORDER BY v.visitDate DESC, v.visitTime DESC LIMIT 1")
    Visit findLastVisitByGuestId(@Param("guestId") Long guestId);
    
    /**
     * Count visits for a guest
     */
    long countByGuestId(Long guestId);
    
    /**
     * Count visits by staff member
     */
    long countByStaffId(Long staffId);
    
    /**
     * Count visits on specific date
     */
    long countByVisitDate(LocalDate visitDate);
    
    /**
     * Count visits in date range
     */
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate")
    long countByVisitDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find visits with service notes containing specific text
     */
    @Query("SELECT v FROM Visit v WHERE LOWER(v.serviceNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByServiceNotesContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Find visits created by specific staff member in date range
     */
    @Query("SELECT v FROM Visit v WHERE v.staff.id = :staffId AND v.visitDate BETWEEN :startDate AND :endDate ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByStaffIdAndDateRange(@Param("staffId") Long staffId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find visits for guest in date range
     */
    @Query("SELECT v FROM Visit v WHERE v.guest.id = :guestId AND v.visitDate BETWEEN :startDate AND :endDate ORDER BY v.visitDate DESC, v.visitTime DESC")
    List<Visit> findByGuestIdAndDateRange(@Param("guestId") Long guestId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Get visit statistics for a guest
     */
    @Query("SELECT " +
           "COUNT(v) as totalVisits, " +
           "MAX(v.visitDate) as lastVisitDate, " +
           "MIN(v.visitDate) as firstVisitDate, " +
           "AVG(v.partySize) as averagePartySize " +
           "FROM Visit v WHERE v.guest.id = :guestId")
    Object[] getGuestVisitStatistics(@Param("guestId") Long guestId);
    
    /**
     * Find frequent guests (guests with more than specified number of visits)
     */
    @Query("SELECT v.guest, COUNT(v) as visitCount FROM Visit v " +
           "GROUP BY v.guest " +
           "HAVING COUNT(v) >= :minVisits " +
           "ORDER BY COUNT(v) DESC")
    List<Object[]> findFrequentGuests(@Param("minVisits") Long minVisits);
    
    /**
     * Find busiest days (dates with most visits)
     */
    @Query("SELECT v.visitDate, COUNT(v) as visitCount FROM Visit v " +
           "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
           "GROUP BY v.visitDate " +
           "ORDER BY COUNT(v) DESC")
    List<Object[]> findBusiestDays(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find visits created or updated recently
     */
    @Query("SELECT v FROM Visit v WHERE v.createdAt >= :since OR v.updatedAt >= :since ORDER BY v.updatedAt DESC")
    List<Visit> findRecentlyModified(@Param("since") LocalDateTime since);
    
    /**
     * Advanced visit search
     */
    @Query("SELECT v FROM Visit v WHERE " +
           "(:guestId IS NULL OR v.guest.id = :guestId) " +
           "AND (:staffId IS NULL OR v.staff.id = :staffId) " +
           "AND (:startDate IS NULL OR v.visitDate >= :startDate) " +
           "AND (:endDate IS NULL OR v.visitDate <= :endDate) " +
           "AND (:tableNumber IS NULL OR v.tableNumber = :tableNumber) " +
           "AND (:minPartySize IS NULL OR v.partySize >= :minPartySize) " +
           "AND (:maxPartySize IS NULL OR v.partySize <= :maxPartySize) " +
           "ORDER BY v.visitDate DESC, v.visitTime DESC")
    Page<Visit> advancedSearch(
        @Param("guestId") Long guestId,
        @Param("staffId") Long staffId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("tableNumber") String tableNumber,
        @Param("minPartySize") Integer minPartySize,
        @Param("maxPartySize") Integer maxPartySize,
        Pageable pageable
    );
}