package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    
    /**
     * Find guest by phone number (active guests only)
     */
    Optional<Guest> findByPhone(String phone);
    
    /**
     * Find guest by phone number including deleted guests
     */
    @Query("SELECT g FROM Guest g WHERE g.phone = :phone")
    Optional<Guest> findByPhoneIncludingDeleted(@Param("phone") String phone);
    
    /**
     * Check if phone number exists (for duplicate validation)
     */
    boolean existsByPhone(String phone);
    
    /**
     * Check if phone exists for different guest (for update validation)
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Guest g WHERE g.phone = :phone AND g.id != :id AND g.deletedAt IS NULL")
    boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("id") Long id);
    
    /**
     * Search guests by name with pagination
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(CONCAT(g.firstName, ' ', COALESCE(g.lastName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(COALESCE(g.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Guest> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Search guests by phone number with pagination
     */
    @Query("SELECT g FROM Guest g WHERE g.phone LIKE CONCAT('%', :phone, '%')")
    Page<Guest> searchByPhone(@Param("phone") String phone, Pageable pageable);
    
    /**
     * Search guests by name or phone with pagination
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(CONCAT(g.firstName, ' ', COALESCE(g.lastName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(COALESCE(g.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR g.phone LIKE CONCAT('%', :searchTerm, '%')")
    Page<Guest> searchByNameOrPhone(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find guests with specific dietary restriction
     */
    @Query("SELECT g FROM Guest g WHERE :restriction = ANY(g.dietaryRestrictions)")
    List<Guest> findByDietaryRestriction(@Param("restriction") String restriction);
    
    /**
     * Find guests with any of the specified dietary restrictions
     */
    @Query("SELECT DISTINCT g FROM Guest g WHERE EXISTS " +
           "(SELECT 1 FROM unnest(g.dietaryRestrictions) AS restriction WHERE restriction IN :restrictions)")
    List<Guest> findByDietaryRestrictionsIn(@Param("restrictions") List<String> restrictions);
    
    /**
     * Find guests with specific favorite drink
     */
    @Query("SELECT g FROM Guest g WHERE :drink = ANY(g.favoriteDrinks)")
    List<Guest> findByFavoriteDrink(@Param("drink") String drink);
    
    /**
     * Find guests with specific seating preference
     */
    List<Guest> findBySeatingPreference(String seatingPreference);
    
    /**
     * Find guests with birthdays in current month
     */
    @Query("SELECT g FROM Guest g WHERE EXTRACT(MONTH FROM g.birthday) = EXTRACT(MONTH FROM CURRENT_DATE)")
    List<Guest> findGuestsWithBirthdayThisMonth();
    
    /**
     * Find guests with anniversaries in current month
     */
    @Query("SELECT g FROM Guest g WHERE EXTRACT(MONTH FROM g.anniversary) = EXTRACT(MONTH FROM CURRENT_DATE)")
    List<Guest> findGuestsWithAnniversaryThisMonth();
    
    /**
     * Find guests with birthdays in date range
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "(EXTRACT(MONTH FROM g.birthday) = EXTRACT(MONTH FROM :startDate) AND EXTRACT(DAY FROM g.birthday) >= EXTRACT(DAY FROM :startDate)) " +
           "OR (EXTRACT(MONTH FROM g.birthday) = EXTRACT(MONTH FROM :endDate) AND EXTRACT(DAY FROM g.birthday) <= EXTRACT(DAY FROM :endDate)) " +
           "OR (EXTRACT(MONTH FROM g.birthday) > EXTRACT(MONTH FROM :startDate) AND EXTRACT(MONTH FROM g.birthday) < EXTRACT(MONTH FROM :endDate))")
    List<Guest> findGuestsWithBirthdayInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find guests with anniversaries in date range
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "(EXTRACT(MONTH FROM g.anniversary) = EXTRACT(MONTH FROM :startDate) AND EXTRACT(DAY FROM g.anniversary) >= EXTRACT(DAY FROM :startDate)) " +
           "OR (EXTRACT(MONTH FROM g.anniversary) = EXTRACT(MONTH FROM :endDate) AND EXTRACT(DAY FROM g.anniversary) <= EXTRACT(DAY FROM :endDate)) " +
           "OR (EXTRACT(MONTH FROM g.anniversary) > EXTRACT(MONTH FROM :startDate) AND EXTRACT(MONTH FROM g.anniversary) < EXTRACT(MONTH FROM :endDate))")
    List<Guest> findGuestsWithAnniversaryInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Find guests with upcoming special occasions (next 30 days)
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "g.birthday IS NOT NULL AND " +
           "((EXTRACT(MONTH FROM g.birthday) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(DAY FROM g.birthday) >= EXTRACT(DAY FROM CURRENT_DATE)) " +
           "OR (EXTRACT(MONTH FROM g.birthday) = EXTRACT(MONTH FROM CURRENT_DATE + INTERVAL '30 days') AND EXTRACT(DAY FROM g.birthday) <= EXTRACT(DAY FROM CURRENT_DATE + INTERVAL '30 days'))) " +
           "OR " +
           "g.anniversary IS NOT NULL AND " +
           "((EXTRACT(MONTH FROM g.anniversary) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(DAY FROM g.anniversary) >= EXTRACT(DAY FROM CURRENT_DATE)) " +
           "OR (EXTRACT(MONTH FROM g.anniversary) = EXTRACT(MONTH FROM CURRENT_DATE + INTERVAL '30 days') AND EXTRACT(DAY FROM g.anniversary) <= EXTRACT(DAY FROM CURRENT_DATE + INTERVAL '30 days')))")
    List<Guest> findGuestsWithUpcomingOccasions();
    
    /**
     * Find guests who haven't visited recently (returning guests)
     */
    @Query("SELECT g FROM Guest g WHERE g.id NOT IN " +
           "(SELECT DISTINCT v.guest.id FROM Visit v WHERE v.visitDate > :cutoffDate)")
    List<Guest> findReturningGuests(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Find guests with upcoming birthdays (for notifications)
     */
    @Query("SELECT g FROM Guest g WHERE g.birthday IS NOT NULL AND " +
           "((EXTRACT(MONTH FROM g.birthday) = :startMonth AND EXTRACT(DAY FROM g.birthday) >= :startDay) " +
           "OR (EXTRACT(MONTH FROM g.birthday) = :endMonth AND EXTRACT(DAY FROM g.birthday) <= :endDay) " +
           "OR (EXTRACT(MONTH FROM g.birthday) > :startMonth AND EXTRACT(MONTH FROM g.birthday) < :endMonth))")
    List<Guest> findGuestsWithUpcomingBirthdays(
        @Param("startMonth") int startMonth, 
        @Param("startDay") int startDay,
        @Param("endMonth") int endMonth, 
        @Param("endDay") int endDay
    );
    
    /**
     * Find guests with upcoming anniversaries (for notifications)
     */
    @Query("SELECT g FROM Guest g WHERE g.anniversary IS NOT NULL AND " +
           "((EXTRACT(MONTH FROM g.anniversary) = :startMonth AND EXTRACT(DAY FROM g.anniversary) >= :startDay) " +
           "OR (EXTRACT(MONTH FROM g.anniversary) = :endMonth AND EXTRACT(DAY FROM g.anniversary) <= :endDay) " +
           "OR (EXTRACT(MONTH FROM g.anniversary) > :startMonth AND EXTRACT(MONTH FROM g.anniversary) < :endMonth))")
    List<Guest> findGuestsWithUpcomingAnniversaries(
        @Param("startMonth") int startMonth, 
        @Param("startDay") int startDay,
        @Param("endMonth") int endMonth, 
        @Param("endDay") int endDay
    );
    
    /**
     * Find guests created by specific staff member
     */
    List<Guest> findByCreatedById(Long staffId);
    
    /**
     * Find recently created guests
     */
    @Query("SELECT g FROM Guest g WHERE g.createdAt >= :since ORDER BY g.createdAt DESC")
    List<Guest> findRecentlyCreated(@Param("since") LocalDateTime since);
    
    /**
     * Soft delete guest
     */
    @Modifying
    @Query("UPDATE Guest g SET g.deletedAt = :deletedAt WHERE g.id = :id")
    void softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);
    
    /**
     * Restore soft deleted guest
     */
    @Modifying
    @Query("UPDATE Guest g SET g.deletedAt = NULL WHERE g.id = :id")
    void restore(@Param("id") Long id);
    
    /**
     * Count active guests
     */
    @Query("SELECT COUNT(g) FROM Guest g WHERE g.deletedAt IS NULL")
    long countActiveGuests();
    
    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT g FROM Guest g WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(CONCAT(g.firstName, ' ', COALESCE(g.lastName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(COALESCE(g.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR g.phone LIKE CONCAT('%', :searchTerm, '%')) " +
           "AND (:seatingPreference IS NULL OR g.seatingPreference = :seatingPreference) " +
           "AND (:hasBirthday IS NULL OR (:hasBirthday = true AND g.birthday IS NOT NULL) OR (:hasBirthday = false AND g.birthday IS NULL)) " +
           "AND (:hasAnniversary IS NULL OR (:hasAnniversary = true AND g.anniversary IS NOT NULL) OR (:hasAnniversary = false AND g.anniversary IS NULL))")
    Page<Guest> advancedSearch(
        @Param("searchTerm") String searchTerm,
        @Param("seatingPreference") String seatingPreference,
        @Param("hasBirthday") Boolean hasBirthday,
        @Param("hasAnniversary") Boolean hasAnniversary,
        Pageable pageable
    );
    
    /**
     * Find guests by multiple dietary restrictions (OR condition)
     */
    @Query("SELECT DISTINCT g FROM Guest g WHERE " +
           "EXISTS (SELECT 1 FROM unnest(g.dietaryRestrictions) AS restriction WHERE restriction IN :restrictions)")
    Page<Guest> findByDietaryRestrictionsIn(@Param("restrictions") List<String> restrictions, Pageable pageable);
    
    /**
     * Find guests by multiple favorite drinks (OR condition)
     */
    @Query("SELECT DISTINCT g FROM Guest g WHERE " +
           "EXISTS (SELECT 1 FROM unnest(g.favoriteDrinks) AS drink WHERE drink IN :drinks)")
    Page<Guest> findByFavoriteDrinksIn(@Param("drinks") List<String> drinks, Pageable pageable);
    
    /**
     * Complex search with all possible filters
     */
    @Query("SELECT DISTINCT g FROM Guest g WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(CONCAT(g.firstName, ' ', COALESCE(g.lastName, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(COALESCE(g.lastName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR g.phone LIKE CONCAT('%', :searchTerm, '%')) " +
           "AND (:seatingPreference IS NULL OR g.seatingPreference = :seatingPreference) " +
           "AND (:hasBirthday IS NULL OR (:hasBirthday = true AND g.birthday IS NOT NULL) OR (:hasBirthday = false AND g.birthday IS NULL)) " +
           "AND (:hasAnniversary IS NULL OR (:hasAnniversary = true AND g.anniversary IS NOT NULL) OR (:hasAnniversary = false AND g.anniversary IS NULL)) " +
           "AND (:dietaryRestrictions IS NULL OR :dietaryRestrictions IS EMPTY OR " +
           "EXISTS (SELECT 1 FROM unnest(g.dietaryRestrictions) AS restriction WHERE restriction IN :dietaryRestrictions)) " +
           "AND (:favoriteDrinks IS NULL OR :favoriteDrinks IS EMPTY OR " +
           "EXISTS (SELECT 1 FROM unnest(g.favoriteDrinks) AS drink WHERE drink IN :favoriteDrinks))")
    Page<Guest> complexSearch(
        @Param("searchTerm") String searchTerm,
        @Param("seatingPreference") String seatingPreference,
        @Param("hasBirthday") Boolean hasBirthday,
        @Param("hasAnniversary") Boolean hasAnniversary,
        @Param("dietaryRestrictions") List<String> dietaryRestrictions,
        @Param("favoriteDrinks") List<String> favoriteDrinks,
        Pageable pageable
    );
}