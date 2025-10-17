package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GuestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GuestRepository guestRepository;

    private Staff testStaff;
    private Guest testGuest1;
    private Guest testGuest2;
    private Guest testGuest3;

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
        testGuest1.setSeatingPreference("Window table");
        testGuest1.setDietaryRestrictions(Arrays.asList("Vegetarian", "No nuts"));
        testGuest1.setFavoriteDrinks(Arrays.asList("Red wine", "Sparkling water"));
        testGuest1.setBirthday(LocalDate.of(1990, 6, 15));
        testGuest1.setAnniversary(LocalDate.of(2015, 9, 20));
        testGuest1.setCreatedBy(testStaff);
        testGuest1 = entityManager.persistAndFlush(testGuest1);

        testGuest2 = new Guest();
        testGuest2.setFirstName("Jane");
        testGuest2.setLastName("Smith");
        testGuest2.setPhone("+1987654321");
        testGuest2.setEmail("jane.smith@example.com");
        testGuest2.setSeatingPreference("Patio");
        testGuest2.setDietaryRestrictions(Arrays.asList("Gluten-free"));
        testGuest2.setFavoriteDrinks(Arrays.asList("White wine"));
        testGuest2.setBirthday(LocalDate.now().plusDays(5)); // Birthday in 5 days
        testGuest2.setCreatedBy(testStaff);
        testGuest2 = entityManager.persistAndFlush(testGuest2);

        testGuest3 = new Guest();
        testGuest3.setFirstName("Bob");
        testGuest3.setLastName("Johnson");
        testGuest3.setPhone("+1122334455");
        testGuest3.setEmail("bob.johnson@example.com");
        testGuest3.setAnniversary(LocalDate.now().plusDays(10)); // Anniversary in 10 days
        testGuest3.setCreatedBy(testStaff);
        testGuest3 = entityManager.persistAndFlush(testGuest3);

        entityManager.clear();
    }

    @Test
    void findByPhone_Success() {
        // Act
        Optional<Guest> result = guestRepository.findByPhone("+1234567890");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGuest1.getId(), result.get().getId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    @Test
    void findByPhone_NotFound() {
        // Act
        Optional<Guest> result = guestRepository.findByPhone("+9999999999");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void existsByPhone_True() {
        // Act
        boolean result = guestRepository.existsByPhone("+1234567890");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByPhone_False() {
        // Act
        boolean result = guestRepository.existsByPhone("+9999999999");

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByPhoneAndIdNot_True() {
        // Act
        boolean result = guestRepository.existsByPhoneAndIdNot("+1234567890", testGuest2.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByPhoneAndIdNot_False_SameGuest() {
        // Act
        boolean result = guestRepository.existsByPhoneAndIdNot("+1234567890", testGuest1.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByPhoneAndIdNot_False_PhoneNotExists() {
        // Act
        boolean result = guestRepository.existsByPhoneAndIdNot("+9999999999", testGuest1.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void searchByName_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.searchByName("John", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchByName_PartialMatch() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.searchByName("Jo", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // John and Johnson
    }

    @Test
    void searchByPhone_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.searchByPhone("1234", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchByNameOrPhone_ByName() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.searchByNameOrPhone("Jane", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest2.getId(), result.getContent().get(0).getId());
    }

    @Test
    void searchByNameOrPhone_ByPhone() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.searchByNameOrPhone("1987", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest2.getId(), result.getContent().get(0).getId());
    }

    @Test
    void findByDietaryRestriction_Success() {
        // Act
        List<Guest> result = guestRepository.findByDietaryRestriction("Vegetarian");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGuest1.getId(), result.get(0).getId());
    }

    @Test
    void findByDietaryRestrictionsIn_Success() {
        // Act
        List<Guest> result = guestRepository.findByDietaryRestrictionsIn(Arrays.asList("Vegetarian", "Gluten-free"));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(testGuest1.getId())));
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(testGuest2.getId())));
    }

    @Test
    void findByFavoriteDrink_Success() {
        // Act
        List<Guest> result = guestRepository.findByFavoriteDrink("Red wine");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGuest1.getId(), result.get(0).getId());
    }

    @Test
    void findBySeatingPreference_Success() {
        // Act
        List<Guest> result = guestRepository.findBySeatingPreference("Window table");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGuest1.getId(), result.get(0).getId());
    }

    @Test
    void findGuestsWithBirthdayThisMonth_Success() {
        // Act
        List<Guest> result = guestRepository.findGuestsWithBirthdayThisMonth();

        // Assert
        assertNotNull(result);
        // Should include testGuest2 if current month matches birthday month
        if (LocalDate.now().getMonth() == testGuest2.getBirthday().getMonth()) {
            assertEquals(1, result.size());
            assertEquals(testGuest2.getId(), result.get(0).getId());
        }
    }

    @Test
    void findGuestsWithAnniversaryThisMonth_Success() {
        // Act
        List<Guest> result = guestRepository.findGuestsWithAnniversaryThisMonth();

        // Assert
        assertNotNull(result);
        // Should include testGuest3 if current month matches anniversary month
        if (LocalDate.now().getMonth() == testGuest3.getAnniversary().getMonth()) {
            assertEquals(1, result.size());
            assertEquals(testGuest3.getId(), result.get(0).getId());
        }
    }

    @Test
    void findGuestsWithUpcomingBirthdays_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(7);

        // Act
        List<Guest> result = guestRepository.findGuestsWithUpcomingBirthdays(
                today.getMonthValue(), today.getDayOfMonth(),
                futureDate.getMonthValue(), futureDate.getDayOfMonth()
        );

        // Assert
        assertNotNull(result);
        // Should include testGuest2 since birthday is in 5 days
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(testGuest2.getId())));
    }

    @Test
    void findGuestsWithUpcomingAnniversaries_Success() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(15);

        // Act
        List<Guest> result = guestRepository.findGuestsWithUpcomingAnniversaries(
                today.getMonthValue(), today.getDayOfMonth(),
                futureDate.getMonthValue(), futureDate.getDayOfMonth()
        );

        // Assert
        assertNotNull(result);
        // Should include testGuest3 since anniversary is in 10 days
        assertTrue(result.stream().anyMatch(g -> g.getId().equals(testGuest3.getId())));
    }

    @Test
    void findByCreatedById_Success() {
        // Act
        List<Guest> result = guestRepository.findByCreatedById(testStaff.getId());

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // All test guests created by testStaff
    }

    @Test
    void findRecentlyCreated_Success() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(1);

        // Act
        List<Guest> result = guestRepository.findRecentlyCreated(since);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // All test guests created recently
    }

    @Test
    void countActiveGuests_Success() {
        // Act
        long count = guestRepository.countActiveGuests();

        // Assert
        assertEquals(3, count); // All test guests are active (not soft deleted)
    }

    @Test
    void advancedSearch_BySearchTerm_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.advancedSearch("John", null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void advancedSearch_BySeatingPreference_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.advancedSearch(null, "Window table", null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void advancedSearch_ByHasBirthday_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.advancedSearch(null, null, true, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // testGuest1 and testGuest2 have birthdays
    }

    @Test
    void advancedSearch_ByHasAnniversary_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.advancedSearch(null, null, null, true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size()); // testGuest1 and testGuest3 have anniversaries
    }

    @Test
    void advancedSearch_MultipleFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.advancedSearch("John", "Window table", true, true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void findByDietaryRestrictionsIn_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.findByDietaryRestrictionsIn(
                Arrays.asList("Vegetarian", "Gluten-free"), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findByFavoriteDrinksIn_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.findByFavoriteDrinksIn(
                Arrays.asList("Red wine", "White wine"), pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void complexSearch_AllFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.complexSearch(
                "John",
                "Window table",
                true,
                true,
                Arrays.asList("Vegetarian"),
                Arrays.asList("Red wine"),
                pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest1.getId(), result.getContent().get(0).getId());
    }

    @Test
    void complexSearch_NullFilters_ReturnsAll() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Guest> result = guestRepository.complexSearch(null, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
    }

    @Test
    void softDelete_Success() {
        // Arrange
        LocalDateTime deletedAt = LocalDateTime.now();

        // Act
        guestRepository.softDelete(testGuest1.getId(), deletedAt);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Guest> result = guestRepository.findById(testGuest1.getId());
        assertTrue(result.isPresent());
        assertNotNull(result.get().getDeletedAt());
    }

    @Test
    void restore_Success() {
        // Arrange
        LocalDateTime deletedAt = LocalDateTime.now();
        guestRepository.softDelete(testGuest1.getId(), deletedAt);
        entityManager.flush();

        // Act
        guestRepository.restore(testGuest1.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Guest> result = guestRepository.findById(testGuest1.getId());
        assertTrue(result.isPresent());
        assertNull(result.get().getDeletedAt());
    }
}