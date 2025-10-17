package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import com.restaurant.vip.entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VisitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitRepository visitRepository;

    private Staff testStaff;
    private Guest testGuest1;
    private Guest testGuest2;
    private Visit visit1;
    private Visit visit2;
    private Visit visit3;

    @BeforeEach
    void setUp() {
        // Create test staff
        testStaff = new Staff();
        testStaff.setEmail("test@restaurant.com");
        testStaff.setPasswordHash("hashedpassword");
        testStaff.setFirstName("John");
        testStaff.setLastName("Staff");
        testStaff.setRole(StaffRole.SERVER);
        testStaff.setActive(true);
        testStaff = entityManager.persistAndFlush(testStaff);

        // Create test guests
        testGuest1 = new Guest();
        testGuest1.setFirstName("John");
        testGuest1.setLastName("Doe");
        testGuest1.setPhone("+1234567890");
        testGuest1.setEmail("john.doe@example.com");
        testGuest1.setCreatedBy(testStaff);
        testGuest1 = entityManager.persistAndFlush(testGuest1);

        testGuest2 = new Guest();
        testGuest2.setFirstName("Jane");
        testGuest2.setLastName("Smith");
        testGuest2.setPhone("+1987654321");
        testGuest2.setEmail("jane.smith@example.com");
        testGuest2.setCreatedBy(testStaff);
        testGuest2 = entityManager.persistAndFlush(testGuest2);

        // Create test visits
        visit1 = new Visit();
        visit1.setGuest(testGuest1);
        visit1.setStaff(testStaff);
        visit1.setVisitDate(LocalDate.now());
        visit1.setVisitTime(LocalTime.of(19, 30));
        visit1.setPartySize(2);
        visit1.setTableNumber("A5");
        visit1.setServiceNotes("Great service");
        visit1 = entityManager.persistAndFlush(visit1);

        visit2 = new Visit();
        visit2.setGuest(testGuest1);
        visit2.setStaff(testStaff);
        visit2.setVisitDate(LocalDate.now().minusDays(7));
        visit2.setVisitTime(LocalTime.of(18, 0));
        visit2.setPartySize(4);
        visit2.setTableNumber("B3");
        visit2.setServiceNotes("Birthday celebration");
        visit2 = entityManager.persistAndFlush(visit2);

        visit3 = new Visit();
        visit3.setGuest(testGuest2);
        visit3.setStaff(testStaff);
        visit3.setVisitDate(LocalDate.now().minusDays(1));
        visit3.setVisitTime(LocalTime.of(20, 15));
        visit3.setPartySize(2);
        visit3.setTableNumber("C1");
        visit3.setServiceNotes("Excellent food");
        visit3 = entityManager.persistAndFlush(visit3);

        entityManager.clear();
    }

    @Test
    void findByGuestIdOrderByVisitDateDescVisitTimeDesc_Success() {
        // Act
        List<Visit> result = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(testGuest1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be ordered by date desc, time desc
        assertEquals(visit1.getId(), result.get(0).getId()); // Most recent visit first
        assertEquals(visit2.getId(), result.get(1).getId());
    }

    @Test
    void findByGuestIdOrderByVisitDateDescVisitTimeDesc_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Visit> result = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(testGuest1.getId(), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(visit1.getId(), result.getContent().get(0).getId()); // Most recent visit
    }

    @Test
    void findByStaffIdOrderByVisitDateDescVisitTimeDesc_Success() {
        // Act
        List<Visit> result = visitRepository.findByStaffIdOrderByVisitDateDescVisitTimeDesc(testStaff.getId());

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // All visits by testStaff
    }

    @Test
    void findByVisitDateOrderByVisitTimeDesc_Success() {
        // Act
        List<Visit> result = visitRepository.findByVisitDateOrderByVisitTimeDesc(LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByVisitDateOrderByVisitTimeAsc_Success() {
        // Act
        List<Visit> result = visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByVisitDateBetween_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        List<Visit> result = visitRepository.findByVisitDateBetween(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // All visits within range
    }

    @Test
    void findByVisitDateBetween_WithPagination_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 2);

        // Act
        Page<Visit> result = visitRepository.findByVisitDateBetween(startDate, endDate, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void findRecentVisits_Success() {
        // Arrange
        LocalDate cutoffDate = LocalDate.now().minusDays(5);

        // Act
        List<Visit> result = visitRepository.findRecentVisits(cutoffDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // visit1 and visit3 are within 5 days
    }

    @Test
    void findTodaysVisits_Success() {
        // Act
        List<Visit> result = visitRepository.findTodaysVisits();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByTableNumberOrderByVisitDateDescVisitTimeDesc_Success() {
        // Act
        List<Visit> result = visitRepository.findByTableNumberOrderByVisitDateDescVisitTimeDesc("A5");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByPartySizeGreaterThanEqual_Success() {
        // Act
        List<Visit> result = visitRepository.findByPartySizeGreaterThanEqual(3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit2.getId(), result.get(0).getId()); // Party size 4
    }

    @Test
    void findLastVisitByGuestId_Success() {
        // Act
        Visit result = visitRepository.findLastVisitByGuestId(testGuest1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(visit1.getId(), result.getId()); // Most recent visit
    }

    @Test
    void findLastVisitByGuestId_NoVisits_ReturnsNull() {
        // Arrange
        Guest guestWithNoVisits = new Guest();
        guestWithNoVisits.setFirstName("No");
        guestWithNoVisits.setLastName("Visits");
        guestWithNoVisits.setPhone("+1000000000");
        guestWithNoVisits.setCreatedBy(testStaff);
        guestWithNoVisits = entityManager.persistAndFlush(guestWithNoVisits);

        // Act
        Visit result = visitRepository.findLastVisitByGuestId(guestWithNoVisits.getId());

        // Assert
        assertNull(result);
    }

    @Test
    void countByGuestId_Success() {
        // Act
        long count = visitRepository.countByGuestId(testGuest1.getId());

        // Assert
        assertEquals(2, count);
    }

    @Test
    void countByStaffId_Success() {
        // Act
        long count = visitRepository.countByStaffId(testStaff.getId());

        // Assert
        assertEquals(3, count);
    }

    @Test
    void countByVisitDate_Success() {
        // Act
        long count = visitRepository.countByVisitDate(LocalDate.now());

        // Assert
        assertEquals(1, count);
    }

    @Test
    void countByVisitDateBetween_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        long count = visitRepository.countByVisitDateBetween(startDate, endDate);

        // Assert
        assertEquals(3, count);
    }

    @Test
    void findByServiceNotesContaining_Success() {
        // Act
        List<Visit> result = visitRepository.findByServiceNotesContaining("service");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByServiceNotesContaining_CaseInsensitive() {
        // Act
        List<Visit> result = visitRepository.findByServiceNotesContaining("GREAT");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(visit1.getId(), result.get(0).getId());
    }

    @Test
    void findByStaffIdAndDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        List<Visit> result = visitRepository.findByStaffIdAndDateRange(testStaff.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void findByGuestIdAndDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        List<Visit> result = visitRepository.findByGuestIdAndDateRange(testGuest1.getId(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getGuestVisitStatistics_Success() {
        // Act
        Object[] result = visitRepository.getGuestVisitStatistics(testGuest1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals(2L, ((Number) result[0]).longValue()); // Total visits
        assertEquals(LocalDate.now(), result[1]); // Last visit date
        assertEquals(LocalDate.now().minusDays(7), result[2]); // First visit date
        assertEquals(3.0, ((Number) result[3]).doubleValue()); // Average party size (2+4)/2
    }

    @Test
    void findFrequentGuests_Success() {
        // Act
        List<Object[]> result = visitRepository.findFrequentGuests(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only testGuest1 has 2+ visits
        assertEquals(testGuest1.getId(), ((Guest) result.get(0)[0]).getId());
        assertEquals(2L, ((Number) result.get(0)[1]).longValue());
    }

    @Test
    void findBusiestDays_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        // Act
        List<Object[]> result = visitRepository.findBusiestDays(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        // Each result should have date and count
        for (Object[] row : result) {
            assertNotNull(row[0]); // Date
            assertNotNull(row[1]); // Count
        }
    }

    @Test
    void findRecentlyModified_Success() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(1);

        // Act
        List<Visit> result = visitRepository.findRecentlyModified(since);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // All visits created recently
    }

    @Test
    void advancedSearch_ByGuestId_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                testGuest1.getId(), null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void advancedSearch_ByStaffId_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                null, testStaff.getId(), null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    @Test
    void advancedSearch_ByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                null, null, startDate, endDate, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // visit1 and visit3
    }

    @Test
    void advancedSearch_ByTableNumber_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                null, null, null, null, "A5", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(visit1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void advancedSearch_ByPartySize_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                null, null, null, null, null, 3, 5, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(visit2.getId(), result.getContent().get(0).getId()); // Party size 4
    }

    @Test
    void advancedSearch_MultipleFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                testGuest1.getId(), testStaff.getId(), LocalDate.now().minusDays(1), 
                LocalDate.now(), null, 1, 3, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(visit1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void advancedSearch_NoFilters_ReturnsAll() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Visit> result = visitRepository.advancedSearch(
                null, null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }
}