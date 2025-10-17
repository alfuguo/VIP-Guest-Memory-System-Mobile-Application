package com.restaurant.vip.service;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import com.restaurant.vip.exception.DuplicateGuestException;
import com.restaurant.vip.exception.ResourceNotFoundException;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private DuplicateDetectionService duplicateDetectionService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GuestService guestService;

    private Staff testStaff;
    private Guest testGuest;
    private GuestCreateRequest createRequest;
    private GuestUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test staff
        testStaff = new Staff();
        testStaff.setId(1L);
        testStaff.setEmail("test@restaurant.com");
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setRole(StaffRole.SERVER);
        testStaff.setActive(true);

        // Setup test guest
        testGuest = new Guest();
        testGuest.setId(1L);
        testGuest.setFirstName("Jane");
        testGuest.setLastName("Smith");
        testGuest.setPhone("+1234567890");
        testGuest.setEmail("jane@example.com");
        testGuest.setCreatedBy(testStaff);
        testGuest.setCreatedAt(LocalDateTime.now());
        testGuest.setUpdatedAt(LocalDateTime.now());

        // Setup create request
        createRequest = new GuestCreateRequest();
        createRequest.setFirstName("Jane");
        createRequest.setLastName("Smith");
        createRequest.setPhone("+1234567890");
        createRequest.setEmail("jane@example.com");
        createRequest.setSeatingPreference("Window table");
        createRequest.setDietaryRestrictions(Arrays.asList("Vegetarian"));
        createRequest.setFavoriteDrinks(Arrays.asList("Red wine"));
        createRequest.setBirthday(LocalDate.of(1990, 5, 15));

        // Setup update request
        updateRequest = new GuestUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setPhone("+1234567890");
        updateRequest.setEmail("jane.updated@example.com");
        updateRequest.setSeatingPreference("Patio");

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@restaurant.com");
    }

    @Test
    void createGuest_Success() {
        // Arrange
        when(guestRepository.existsByPhone(createRequest.getPhone())).thenReturn(false);
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);

        // Act
        GuestResponse result = guestService.createGuest(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testGuest.getId(), result.getId());
        assertEquals(testGuest.getFirstName(), result.getFirstName());
        assertEquals(testGuest.getLastName(), result.getLastName());
        assertEquals(testGuest.getPhone(), result.getPhone());

        verify(guestRepository).existsByPhone(createRequest.getPhone());
        verify(guestRepository).save(any(Guest.class));
        verify(auditLogService).logAction(eq("CREATE_GUEST"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void createGuest_DuplicatePhone_ThrowsException() {
        // Arrange
        when(guestRepository.existsByPhone(createRequest.getPhone())).thenReturn(true);
        when(guestRepository.findByPhone(createRequest.getPhone())).thenReturn(Optional.of(testGuest));

        // Act & Assert
        assertThrows(DuplicateGuestException.class, () -> guestService.createGuest(createRequest));

        verify(guestRepository).existsByPhone(createRequest.getPhone());
        verify(guestRepository, never()).save(any(Guest.class));
    }

    @Test
    void getGuestById_Success() {
        // Arrange
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act
        GuestResponse result = guestService.getGuestById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testGuest.getId(), result.getId());
        assertEquals(testGuest.getFirstName(), result.getFirstName());

        verify(guestRepository).findById(1L);
        verify(auditLogService).logAction(eq("VIEW_GUEST"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void getGuestById_NotFound_ThrowsException() {
        // Arrange
        when(guestRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> guestService.getGuestById(1L));

        verify(guestRepository).findById(1L);
        verify(auditLogService, never()).logAction(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    void updateGuest_Success() {
        // Arrange
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(guestRepository.existsByPhoneAndIdNot(updateRequest.getPhone(), 1L)).thenReturn(false);
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);

        // Act
        GuestResponse result = guestService.updateGuest(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testGuest.getId(), result.getId());

        verify(guestRepository).findById(1L);
        verify(guestRepository).save(any(Guest.class));
        verify(auditLogService).logAction(eq("UPDATE_GUEST"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void updateGuest_DuplicatePhone_ThrowsException() {
        // Arrange
        updateRequest.setPhone("+9876543210"); // Different phone
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(guestRepository.existsByPhoneAndIdNot(updateRequest.getPhone(), 1L)).thenReturn(true);
        
        Guest duplicateGuest = new Guest();
        duplicateGuest.setId(2L);
        when(guestRepository.findByPhone(updateRequest.getPhone())).thenReturn(Optional.of(duplicateGuest));

        // Act & Assert
        assertThrows(DuplicateGuestException.class, () -> guestService.updateGuest(1L, updateRequest));

        verify(guestRepository).findById(1L);
        verify(guestRepository, never()).save(any(Guest.class));
    }

    @Test
    void deleteGuest_Success() {
        // Arrange
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);

        // Act
        guestService.deleteGuest(1L);

        // Assert
        verify(guestRepository).findById(1L);
        verify(guestRepository).save(any(Guest.class));
        verify(auditLogService).logAction(eq("DELETE_GUEST"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void searchGuests_SimpleSearch_Success() {
        // Arrange
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setSearchTerm("Jane");
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("firstName");
        searchRequest.setSortDirection("ASC");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Guest> guestPage = new PageImpl<>(Arrays.asList(testGuest), pageable, 1);
        
        when(guestRepository.searchByNameOrPhone("Jane", pageable)).thenReturn(guestPage);

        // Act
        PagedResponse<GuestResponse> result = guestService.searchGuests(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest.getId(), result.getContent().get(0).getId());

        verify(guestRepository).searchByNameOrPhone("Jane", any(Pageable.class));
    }

    @Test
    void searchGuests_UpcomingOccasions_Success() {
        // Arrange
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setUpcomingOccasions(true);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("firstName");
        searchRequest.setSortDirection("ASC");

        when(guestRepository.findGuestsWithUpcomingOccasions()).thenReturn(Arrays.asList(testGuest));

        // Act
        PagedResponse<GuestResponse> result = guestService.searchGuests(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(guestRepository).findGuestsWithUpcomingOccasions();
    }

    @Test
    void getAllGuests_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Guest> guestPage = new PageImpl<>(Arrays.asList(testGuest), pageable, 1);
        
        when(guestRepository.findAll(any(Pageable.class))).thenReturn(guestPage);

        // Act
        PagedResponse<GuestResponse> result = guestService.getAllGuests(0, 10, "firstName", "ASC");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testGuest.getId(), result.getContent().get(0).getId());

        verify(guestRepository).findAll(any(Pageable.class));
    }

    @Test
    void existsByPhone_Success() {
        // Arrange
        when(guestRepository.existsByPhone("+1234567890")).thenReturn(true);

        // Act
        boolean result = guestService.existsByPhone("+1234567890");

        // Assert
        assertTrue(result);
        verify(guestRepository).existsByPhone("+1234567890");
    }

    @Test
    void findByPhone_Success() {
        // Arrange
        when(guestRepository.findByPhone("+1234567890")).thenReturn(Optional.of(testGuest));

        // Act
        Optional<GuestResponse> result = guestService.findByPhone("+1234567890");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGuest.getId(), result.get().getId());

        verify(guestRepository).findByPhone("+1234567890");
    }

    @Test
    void uploadGuestPhoto_Success() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        FileUploadResponse uploadResponse = new FileUploadResponse();
        uploadResponse.setUrl("http://example.com/photo.jpg");
        uploadResponse.setFilename("photo.jpg");

        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(fileUploadService.uploadGuestPhoto(mockFile, 1L)).thenReturn(uploadResponse);
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act
        GuestResponse result = guestService.uploadGuestPhoto(1L, mockFile);

        // Assert
        assertNotNull(result);
        verify(fileUploadService).uploadGuestPhoto(mockFile, 1L);
        verify(guestRepository).save(any(Guest.class));
        verify(auditLogService).logAction(eq("UPLOAD_PHOTO"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void uploadGuestPhoto_GuestNotFound_ThrowsException() {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(guestRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> guestService.uploadGuestPhoto(1L, mockFile));

        verify(guestRepository).findById(1L);
        verify(fileUploadService, never()).uploadGuestPhoto(any(), anyLong());
    }

    @Test
    void deleteGuestPhoto_Success() {
        // Arrange
        testGuest.setPhotoUrl("http://example.com/photo.jpg");
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(guestRepository.save(any(Guest.class))).thenReturn(testGuest);
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act
        GuestResponse result = guestService.deleteGuestPhoto(1L);

        // Assert
        assertNotNull(result);
        verify(guestRepository).save(any(Guest.class));
        verify(auditLogService).logAction(eq("DELETE_PHOTO"), anyString(), eq(testGuest.getId()), eq(testStaff.getId()));
    }

    @Test
    void checkDuplicateByPhone_Success() {
        // Arrange
        DuplicateCheckResponse expectedResponse = new DuplicateCheckResponse();
        expectedResponse.setIsDuplicate(false);
        
        when(duplicateDetectionService.checkExactDuplicate("+1234567890")).thenReturn(expectedResponse);

        // Act
        DuplicateCheckResponse result = guestService.checkDuplicateByPhone("+1234567890");

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsDuplicate());
        verify(duplicateDetectionService).checkExactDuplicate("+1234567890");
    }

    @Test
    void findPotentialDuplicates_Success() {
        // Arrange
        List<GuestResponse> potentialDuplicates = Arrays.asList(new GuestResponse());
        when(duplicateDetectionService.findPotentialDuplicates(createRequest)).thenReturn(potentialDuplicates);

        // Act
        PotentialDuplicatesResponse result = guestService.findPotentialDuplicates(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPotentialDuplicates().size());
        verify(duplicateDetectionService).findPotentialDuplicates(createRequest);
    }

    @Test
    void comprehensiveDuplicateCheck_Success() {
        // Arrange
        DuplicateCheckResponse expectedResponse = new DuplicateCheckResponse();
        expectedResponse.setIsDuplicate(false);
        
        when(duplicateDetectionService.comprehensiveDuplicateCheck(createRequest)).thenReturn(expectedResponse);

        // Act
        DuplicateCheckResponse result = guestService.comprehensiveDuplicateCheck(createRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsDuplicate());
        verify(duplicateDetectionService).comprehensiveDuplicateCheck(createRequest);
    }
}