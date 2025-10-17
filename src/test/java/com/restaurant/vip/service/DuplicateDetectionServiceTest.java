package com.restaurant.vip.service;

import com.restaurant.vip.dto.DuplicateCheckResponse;
import com.restaurant.vip.dto.GuestCreateRequest;
import com.restaurant.vip.dto.GuestResponse;
import com.restaurant.vip.dto.GuestUpdateRequest;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.GuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateDetectionServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private DuplicateDetectionService duplicateDetectionService;

    private Guest existingGuest;
    private GuestCreateRequest createRequest;
    private GuestUpdateRequest updateRequest;
    private Staff testStaff;

    @BeforeEach
    void setUp() {
        // Setup test staff
        testStaff = new Staff();
        testStaff.setId(1L);
        testStaff.setFirstName("John");
        testStaff.setLastName("Staff");

        // Setup existing guest
        existingGuest = new Guest();
        existingGuest.setId(1L);
        existingGuest.setFirstName("John");
        existingGuest.setLastName("Doe");
        existingGuest.setPhone("+1234567890");
        existingGuest.setEmail("john.doe@example.com");
        existingGuest.setCreatedBy(testStaff);
        existingGuest.setCreatedAt(LocalDateTime.now());
        existingGuest.setUpdatedAt(LocalDateTime.now());

        // Setup create request
        createRequest = new GuestCreateRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setPhone("+1987654321");
        createRequest.setEmail("jane.smith@example.com");

        // Setup update request
        updateRequest = new GuestUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhone("+1987654321");
        updateRequest.setEmail("jane.smith@example.com");
    }

    @Test
    void checkExactDuplicate_Exists_ReturnsTrue() {
        // Arrange
        when(guestRepository.findByPhone("+1234567890")).thenReturn(Optional.of(existingGuest));

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.checkExactDuplicate("+1234567890");

        // Assert
        assertNotNull(result);
        assertEquals("+1234567890", result.getPhone());
        assertTrue(result.isExists());
        assertTrue(result.getMessage().contains("already exists"));
        assertNotNull(result.getExistingGuest());
        assertEquals(existingGuest.getId(), result.getExistingGuest().getId());

        verify(guestRepository).findByPhone("+1234567890");
    }

    @Test
    void checkExactDuplicate_NotExists_ReturnsFalse() {
        // Arrange
        when(guestRepository.findByPhone("+1987654321")).thenReturn(Optional.empty());

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.checkExactDuplicate("+1987654321");

        // Assert
        assertNotNull(result);
        assertEquals("+1987654321", result.getPhone());
        assertFalse(result.isExists());
        assertEquals("No guest found with this phone number", result.getMessage());
        assertNull(result.getExistingGuest());

        verify(guestRepository).findByPhone("+1987654321");
    }

    @Test
    void checkExactDuplicateForUpdate_Exists_ReturnsTrue() {
        // Arrange
        when(guestRepository.existsByPhoneAndIdNot("+1234567890", 2L)).thenReturn(true);
        when(guestRepository.findByPhone("+1234567890")).thenReturn(Optional.of(existingGuest));

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.checkExactDuplicateForUpdate("+1234567890", 2L);

        // Assert
        assertNotNull(result);
        assertEquals("+1234567890", result.getPhone());
        assertTrue(result.isExists());
        assertTrue(result.getMessage().contains("Another guest"));
        assertNotNull(result.getExistingGuest());

        verify(guestRepository).existsByPhoneAndIdNot("+1234567890", 2L);
        verify(guestRepository).findByPhone("+1234567890");
    }

    @Test
    void checkExactDuplicateForUpdate_NotExists_ReturnsFalse() {
        // Arrange
        when(guestRepository.existsByPhoneAndIdNot("+1987654321", 1L)).thenReturn(false);

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.checkExactDuplicateForUpdate("+1987654321", 1L);

        // Assert
        assertNotNull(result);
        assertEquals("+1987654321", result.getPhone());
        assertFalse(result.isExists());
        assertEquals("No other guest found with this phone number", result.getMessage());

        verify(guestRepository).existsByPhoneAndIdNot("+1987654321", 1L);
        verify(guestRepository, never()).findByPhone(anyString());
    }

    @Test
    void findPotentialDuplicates_NameSimilarity_Success() {
        // Arrange
        Guest similarGuest = new Guest();
        similarGuest.setId(2L);
        similarGuest.setFirstName("Jane");
        similarGuest.setLastName("Doe"); // Different last name but similar first name
        similarGuest.setPhone("+1111111111");
        similarGuest.setCreatedBy(testStaff);
        similarGuest.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(similarGuest));
        Page<Guest> phoneMatches = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(phoneMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(similarGuest.getId(), result.get(0).getId());

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(anyString(), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicates_PhoneSimilarity_Success() {
        // Arrange
        Guest phoneGuest = new Guest();
        phoneGuest.setId(3L);
        phoneGuest.setFirstName("Bob");
        phoneGuest.setLastName("Johnson");
        phoneGuest.setPhone("1987654321"); // Similar phone without +
        phoneGuest.setCreatedBy(testStaff);
        phoneGuest.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList());
        Page<Guest> phoneMatches = new PageImpl<>(Arrays.asList(phoneGuest));

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);
        when(guestRepository.searchByPhone(eq("1987654321"), any(PageRequest.class)))
                .thenReturn(phoneMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(phoneGuest.getId(), result.get(0).getId());

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(eq("1987654321"), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicates_NoDuplicates_ReturnsEmpty() {
        // Arrange
        Page<Guest> emptyPage = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(emptyPage);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(anyString(), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicates_ExcludesExactPhoneMatch() {
        // Arrange
        Guest exactPhoneMatch = new Guest();
        exactPhoneMatch.setId(4L);
        exactPhoneMatch.setFirstName("Different");
        exactPhoneMatch.setLastName("Name");
        exactPhoneMatch.setPhone("+1987654321"); // Exact phone match
        exactPhoneMatch.setCreatedBy(testStaff);
        exactPhoneMatch.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(exactPhoneMatch));
        Page<Guest> phoneMatches = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(phoneMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should be empty because exact phone match is excluded

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(anyString(), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicatesForUpdate_Success() {
        // Arrange
        Guest similarGuest = new Guest();
        similarGuest.setId(2L);
        similarGuest.setFirstName("Jane");
        similarGuest.setLastName("Doe");
        similarGuest.setPhone("+1111111111");
        similarGuest.setCreatedBy(testStaff);
        similarGuest.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(similarGuest));

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicatesForUpdate(updateRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(similarGuest.getId(), result.get(0).getId());

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicatesForUpdate_ExcludesCurrentGuest() {
        // Arrange
        Guest currentGuest = new Guest();
        currentGuest.setId(1L); // Same as current guest ID
        currentGuest.setFirstName("Jane");
        currentGuest.setLastName("Smith");
        currentGuest.setPhone("+1111111111");
        currentGuest.setCreatedBy(testStaff);
        currentGuest.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(currentGuest));

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicatesForUpdate(updateRequest, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should be empty because current guest is excluded

        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
    }

    @Test
    void comprehensiveDuplicateCheck_ExactDuplicateExists_ReturnsExactMatch() {
        // Arrange
        createRequest.setPhone("+1234567890"); // Same as existing guest
        when(guestRepository.findByPhone("+1234567890")).thenReturn(Optional.of(existingGuest));

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.comprehensiveDuplicateCheck(createRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isExists());
        assertTrue(result.getMessage().contains("already exists"));
        assertNotNull(result.getExistingGuest());

        verify(guestRepository).findByPhone("+1234567890");
        // Should not check for potential duplicates if exact duplicate exists
        verify(guestRepository, never()).searchByName(anyString(), any(PageRequest.class));
    }

    @Test
    void comprehensiveDuplicateCheck_PotentialDuplicatesFound() {
        // Arrange
        when(guestRepository.findByPhone("+1987654321")).thenReturn(Optional.empty());
        
        Guest potentialDuplicate = new Guest();
        potentialDuplicate.setId(2L);
        potentialDuplicate.setFirstName("Jane");
        potentialDuplicate.setLastName("Doe");
        potentialDuplicate.setPhone("+1111111111");
        potentialDuplicate.setCreatedBy(testStaff);
        potentialDuplicate.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(potentialDuplicate));
        Page<Guest> phoneMatches = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(nameMatches);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(phoneMatches);

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.comprehensiveDuplicateCheck(createRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isExists());
        assertTrue(result.getMessage().contains("potential duplicate"));
        assertNotNull(result.getExistingGuest());
        assertEquals(potentialDuplicate.getId(), result.getExistingGuest().getId());

        verify(guestRepository).findByPhone("+1987654321");
        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(anyString(), any(PageRequest.class));
    }

    @Test
    void comprehensiveDuplicateCheck_NoDuplicatesFound() {
        // Arrange
        when(guestRepository.findByPhone("+1987654321")).thenReturn(Optional.empty());
        
        Page<Guest> emptyPage = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane Smith"), any(PageRequest.class)))
                .thenReturn(emptyPage);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(emptyPage);

        // Act
        DuplicateCheckResponse result = duplicateDetectionService.comprehensiveDuplicateCheck(createRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isExists());
        assertEquals("No duplicates found", result.getMessage());
        assertNull(result.getExistingGuest());

        verify(guestRepository).findByPhone("+1987654321");
        verify(guestRepository).searchByName(eq("Jane Smith"), any(PageRequest.class));
        verify(guestRepository).searchByPhone(anyString(), any(PageRequest.class));
    }

    @Test
    void findPotentialDuplicates_FirstNameOnly_Success() {
        // Arrange
        createRequest.setLastName(null); // Only first name
        
        Guest similarGuest = new Guest();
        similarGuest.setId(2L);
        similarGuest.setFirstName("Jane");
        similarGuest.setLastName("Different");
        similarGuest.setPhone("+1111111111");
        similarGuest.setCreatedBy(testStaff);
        similarGuest.setCreatedAt(LocalDateTime.now());

        Page<Guest> nameMatches = new PageImpl<>(Arrays.asList(similarGuest));
        Page<Guest> phoneMatches = new PageImpl<>(Arrays.asList());

        when(guestRepository.searchByName(eq("Jane"), any(PageRequest.class)))
                .thenReturn(nameMatches);
        when(guestRepository.searchByPhone(anyString(), any(PageRequest.class)))
                .thenReturn(phoneMatches);

        // Act
        List<GuestResponse> result = duplicateDetectionService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(similarGuest.getId(), result.get(0).getId());

        verify(guestRepository).searchByName(eq("Jane"), any(PageRequest.class));
    }
}