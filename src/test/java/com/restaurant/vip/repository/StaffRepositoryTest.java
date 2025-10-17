package com.restaurant.vip.repository;

import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StaffRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaffRepository staffRepository;

    private Staff activeServer;
    private Staff activeManager;
    private Staff inactiveStaff;
    private Staff lockedStaff;

    @BeforeEach
    void setUp() {
        // Create active server
        activeServer = new Staff();
        activeServer.setEmail("server@restaurant.com");
        activeServer.setPasswordHash("hashedpassword");
        activeServer.setFirstName("John");
        activeServer.setLastName("Server");
        activeServer.setRole(StaffRole.SERVER);
        activeServer.setActive(true);
        activeServer.setFailedLoginAttempts(0);
        activeServer = entityManager.persistAndFlush(activeServer);

        // Create active manager
        activeManager = new Staff();
        activeManager.setEmail("manager@restaurant.com");
        activeManager.setPasswordHash("hashedpassword");
        activeManager.setFirstName("Jane");
        activeManager.setLastName("Manager");
        activeManager.setRole(StaffRole.MANAGER);
        activeManager.setActive(true);
        activeManager.setFailedLoginAttempts(1);
        activeManager = entityManager.persistAndFlush(activeManager);

        // Create inactive staff
        inactiveStaff = new Staff();
        inactiveStaff.setEmail("inactive@restaurant.com");
        inactiveStaff.setPasswordHash("hashedpassword");
        inactiveStaff.setFirstName("Bob");
        inactiveStaff.setLastName("Inactive");
        inactiveStaff.setRole(StaffRole.HOST);
        inactiveStaff.setActive(false);
        inactiveStaff.setFailedLoginAttempts(0);
        inactiveStaff = entityManager.persistAndFlush(inactiveStaff);

        // Create locked staff
        lockedStaff = new Staff();
        lockedStaff.setEmail("locked@restaurant.com");
        lockedStaff.setPasswordHash("hashedpassword");
        lockedStaff.setFirstName("Alice");
        lockedStaff.setLastName("Locked");
        lockedStaff.setRole(StaffRole.SERVER);
        lockedStaff.setActive(true);
        lockedStaff.setFailedLoginAttempts(5);
        lockedStaff.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        lockedStaff = entityManager.persistAndFlush(lockedStaff);

        entityManager.clear();
    }

    @Test
    void findByEmail_Success() {
        // Act
        Optional<Staff> result = staffRepository.findByEmail("server@restaurant.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(activeServer.getId(), result.get().getId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Server", result.get().getLastName());
        assertEquals(StaffRole.SERVER, result.get().getRole());
    }

    @Test
    void findByEmail_NotFound() {
        // Act
        Optional<Staff> result = staffRepository.findByEmail("nonexistent@restaurant.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmailAndActive_ActiveStaff_Success() {
        // Act
        Optional<Staff> result = staffRepository.findByEmailAndActive("server@restaurant.com", true);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(activeServer.getId(), result.get().getId());
        assertTrue(result.get().getActive());
    }

    @Test
    void findByEmailAndActive_InactiveStaff_NotFound() {
        // Act
        Optional<Staff> result = staffRepository.findByEmailAndActive("inactive@restaurant.com", true);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmailAndActive_InactiveStaff_Found() {
        // Act
        Optional<Staff> result = staffRepository.findByEmailAndActive("inactive@restaurant.com", false);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(inactiveStaff.getId(), result.get().getId());
        assertFalse(result.get().getActive());
    }

    @Test
    void findByActiveTrue_Success() {
        // Act
        List<Staff> result = staffRepository.findByActiveTrue();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size()); // activeServer, activeManager, lockedStaff
        assertTrue(result.stream().allMatch(Staff::getActive));
    }

    @Test
    void findByRole_Success() {
        // Act
        List<Staff> result = staffRepository.findByRole(StaffRole.SERVER);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // activeServer and lockedStaff
        assertTrue(result.stream().allMatch(s -> s.getRole() == StaffRole.SERVER));
    }

    @Test
    void findByRoleAndActiveTrue_Success() {
        // Act
        List<Staff> result = staffRepository.findByRoleAndActiveTrue(StaffRole.SERVER);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // activeServer and lockedStaff (both active)
        assertTrue(result.stream().allMatch(s -> s.getRole() == StaffRole.SERVER && s.getActive()));
    }

    @Test
    void findByRoleAndActiveTrue_Manager_Success() {
        // Act
        List<Staff> result = staffRepository.findByRoleAndActiveTrue(StaffRole.MANAGER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeManager.getId(), result.get(0).getId());
    }

    @Test
    void existsByEmail_True() {
        // Act
        boolean result = staffRepository.existsByEmail("server@restaurant.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmail_False() {
        // Act
        boolean result = staffRepository.existsByEmail("nonexistent@restaurant.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByEmailAndIdNot_True() {
        // Act
        boolean result = staffRepository.existsByEmailAndIdNot("server@restaurant.com", activeManager.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByEmailAndIdNot_False_SameStaff() {
        // Act
        boolean result = staffRepository.existsByEmailAndIdNot("server@restaurant.com", activeServer.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByEmailAndIdNot_False_EmailNotExists() {
        // Act
        boolean result = staffRepository.existsByEmailAndIdNot("nonexistent@restaurant.com", activeServer.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void findByFailedLoginAttemptsGreaterThanEqual_Success() {
        // Act
        List<Staff> result = staffRepository.findByFailedLoginAttemptsGreaterThanEqual(3);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lockedStaff.getId(), result.get(0).getId());
        assertTrue(result.get(0).getFailedLoginAttempts() >= 3);
    }

    @Test
    void findLockedAccounts_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        List<Staff> result = staffRepository.findLockedAccounts(now);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lockedStaff.getId(), result.get(0).getId());
        assertNotNull(result.get(0).getAccountLockedUntil());
        assertTrue(result.get(0).getAccountLockedUntil().isAfter(now));
    }

    @Test
    void findAccountsToUnlock_Success() {
        // Arrange - Create a staff with expired lock
        Staff expiredLockStaff = new Staff();
        expiredLockStaff.setEmail("expired@restaurant.com");
        expiredLockStaff.setPasswordHash("hashedpassword");
        expiredLockStaff.setFirstName("Expired");
        expiredLockStaff.setLastName("Lock");
        expiredLockStaff.setRole(StaffRole.SERVER);
        expiredLockStaff.setActive(true);
        expiredLockStaff.setFailedLoginAttempts(5);
        expiredLockStaff.setAccountLockedUntil(LocalDateTime.now().minusMinutes(10)); // Expired
        expiredLockStaff = entityManager.persistAndFlush(expiredLockStaff);

        LocalDateTime now = LocalDateTime.now();

        // Act
        List<Staff> result = staffRepository.findAccountsToUnlock(now);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expiredLockStaff.getId(), result.get(0).getId());
        assertTrue(result.get(0).getAccountLockedUntil().isBefore(now) || 
                  result.get(0).getAccountLockedUntil().equals(now));
    }

    @Test
    void resetFailedLoginAttempts_Success() {
        // Arrange
        assertEquals(1, activeManager.getFailedLoginAttempts());

        // Act
        staffRepository.resetFailedLoginAttempts(activeManager.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Staff> updated = staffRepository.findById(activeManager.getId());
        assertTrue(updated.isPresent());
        assertEquals(0, updated.get().getFailedLoginAttempts());
        assertNull(updated.get().getAccountLockedUntil());
    }

    @Test
    void incrementFailedLoginAttempts_Success() {
        // Arrange
        int initialAttempts = activeServer.getFailedLoginAttempts();

        // Act
        staffRepository.incrementFailedLoginAttempts(activeServer.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Staff> updated = staffRepository.findById(activeServer.getId());
        assertTrue(updated.isPresent());
        assertEquals(initialAttempts + 1, updated.get().getFailedLoginAttempts());
    }

    @Test
    void lockAccount_Success() {
        // Arrange
        LocalDateTime lockUntil = LocalDateTime.now().plusHours(1);

        // Act
        staffRepository.lockAccount(activeServer.getId(), lockUntil);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Staff> updated = staffRepository.findById(activeServer.getId());
        assertTrue(updated.isPresent());
        assertNotNull(updated.get().getAccountLockedUntil());
        assertEquals(lockUntil.withNano(0), updated.get().getAccountLockedUntil().withNano(0));
    }

    @Test
    void unlockExpiredAccounts_Success() {
        // Arrange - Create staff with expired lock
        Staff expiredLockStaff = new Staff();
        expiredLockStaff.setEmail("expired2@restaurant.com");
        expiredLockStaff.setPasswordHash("hashedpassword");
        expiredLockStaff.setFirstName("Expired2");
        expiredLockStaff.setLastName("Lock2");
        expiredLockStaff.setRole(StaffRole.SERVER);
        expiredLockStaff.setActive(true);
        expiredLockStaff.setFailedLoginAttempts(5);
        expiredLockStaff.setAccountLockedUntil(LocalDateTime.now().minusMinutes(10));
        expiredLockStaff = entityManager.persistAndFlush(expiredLockStaff);

        LocalDateTime now = LocalDateTime.now();

        // Act
        staffRepository.unlockExpiredAccounts(now);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Staff> updated = staffRepository.findById(expiredLockStaff.getId());
        assertTrue(updated.isPresent());
        assertNull(updated.get().getAccountLockedUntil());
    }

    @Test
    void deactivateStaff_Success() {
        // Arrange
        assertTrue(activeServer.getActive());

        // Act
        staffRepository.deactivateStaff(activeServer.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Staff> updated = staffRepository.findById(activeServer.getId());
        assertTrue(updated.isPresent());
        assertFalse(updated.get().getActive());
    }

    @Test
    void searchByName_Success() {
        // Act
        List<Staff> result = staffRepository.searchByName("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeServer.getId(), result.get(0).getId());
    }

    @Test
    void searchByName_PartialMatch() {
        // Act
        List<Staff> result = staffRepository.searchByName("Jo");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeServer.getId(), result.get(0).getId());
    }

    @Test
    void searchByName_LastName() {
        // Act
        List<Staff> result = staffRepository.searchByName("Manager");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeManager.getId(), result.get(0).getId());
    }

    @Test
    void searchByName_FullName() {
        // Act
        List<Staff> result = staffRepository.searchByName("Jane Manager");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeManager.getId(), result.get(0).getId());
    }

    @Test
    void searchByName_CaseInsensitive() {
        // Act
        List<Staff> result = staffRepository.searchByName("JOHN");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeServer.getId(), result.get(0).getId());
    }

    @Test
    void searchByName_NoMatch() {
        // Act
        List<Staff> result = staffRepository.searchByName("NonExistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}