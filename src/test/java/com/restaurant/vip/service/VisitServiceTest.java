package com.restaurant.vip.service;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.exception.ResourceNotFoundException;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import com.restaurant.vip.repository.VisitRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private VisitService visitService;

    private Staff testStaff;
    private Staff managerStaff;
    private Guest testGuest;
    private Visit testVisit;
    private VisitCreateRequest createRequest;
    private VisitUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test staff
        testStaff = new Staff();
        testStaff.setId(1L);
        testStaff.setEmail("server@restaurant.com");
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setRole(StaffRole.SERVER);
        testStaff.setActive(true);

        // Setup manager staff
        managerStaff = new Staff();
        managerStaff.setId(2L);
        managerStaff.setEmail("manager@restaurant.com");
        managerStaff.setFirstName("Jane");
        managerStaff.setLastName("Manager");
        managerStaff.setRole(StaffRole.MANAGER);
        managerStaff.setActive(true);

        // Setup test guest
        testGuest = new Guest();
        testGuest.setId(1L);
        testGuest.setFirstName("Alice");
        testGuest.setLastName("Smith");
        testGuest.setPhone("+1234567890");

        // Setup test visit
        testVisit = new Visit();
        testVisit.setId(1L);
        testVisit.setGuest(testGuest);
        testVisit.setStaff(testStaff);
        testVisit.setVisitDate(LocalDate.now());
        testVisit.setVisitTime(LocalTime.of(19, 30));
        testVisit.setPartySize(2);
        testVisit.setTableNumber("A5");
        testVisit.setServiceNotes("Great service");
        testVisit.setCreatedAt(LocalDateTime.now());

        // Setup create request
        createRequest = new VisitCreateRequest();
        createRequest.setGuestId(1L);
        createRequest.setVisitDate(LocalDate.now());
        createRequest.setVisitTime(LocalTime.of(19, 30));
        createRequest.setPartySize(2);
        createRequest.setTableNumber("A5");
        createRequest.setServiceNotes("Great service");

        // Setup update request
        updateRequest = new VisitUpdateRequest();
        updateRequest.setVisitDate(LocalDate.now());
        updateRequest.setVisitTime(LocalTime.of(20, 0));
        updateRequest.setPartySize(3);
        updateRequest.setTableNumber("B3");
        updateRequest.setServiceNotes("Updated service notes");

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("server@restaurant.com");
    }

    @Test
    void createVisit_Success() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(visitRepository.save(any(Visit.class))).thenReturn(testVisit);

        // Act
        VisitResponse result = visitService.createVisit(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());
        assertEquals(testVisit.getGuest().getId(), result.getGuestId());
        assertEquals(testVisit.getStaff().getId(), result.getStaffId());

        verify(guestRepository).findById(1L);
        verify(visitRepository).save(any(Visit.class));
        verify(auditLogService).logVisitCreated(testStaff.getId(), testVisit.getId(), testGuest.getId());
    }

    @Test
    void createVisit_GuestNotFound_ThrowsException() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(guestRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> visitService.createVisit(createRequest));

        verify(guestRepository).findById(1L);
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void updateVisit_Success_OriginalStaff() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));
        when(visitRepository.save(any(Visit.class))).thenReturn(testVisit);

        // Act
        VisitResponse result = visitService.updateVisit(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());

        verify(visitRepository).findById(1L);
        verify(visitRepository).save(any(Visit.class));
        verify(auditLogService).logVisitUpdated(testStaff.getId(), 1L, testGuest.getId());
    }

    @Test
    void updateVisit_Success_Manager() {
        // Arrange
        when(authentication.getName()).thenReturn("manager@restaurant.com");
        when(staffRepository.findByEmail("manager@restaurant.com")).thenReturn(Optional.of(managerStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));
        when(visitRepository.save(any(Visit.class))).thenReturn(testVisit);

        // Act
        VisitResponse result = visitService.updateVisit(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());

        verify(visitRepository).findById(1L);
        verify(visitRepository).save(any(Visit.class));
        verify(auditLogService).logVisitUpdated(managerStaff.getId(), 1L, testGuest.getId());
    }

    @Test
    void updateVisit_AccessDenied_DifferentStaff() {
        // Arrange
        Staff differentStaff = new Staff();
        differentStaff.setId(3L);
        differentStaff.setRole(StaffRole.SERVER);
        
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(differentStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> visitService.updateVisit(1L, updateRequest));

        verify(visitRepository).findById(1L);
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void getVisitById_Success() {
        // Arrange
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        VisitResponse result = visitService.getVisitById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());
        assertEquals(testVisit.getGuest().getId(), result.getGuestId());

        verify(visitRepository).findById(1L);
    }

    @Test
    void getVisitById_NotFound_ThrowsException() {
        // Arrange
        when(visitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> visitService.getVisitById(1L));

        verify(visitRepository).findById(1L);
    }

    @Test
    void getGuestVisits_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visit> visitPage = new PageImpl<>(Arrays.asList(testVisit), pageable, 1);
        
        when(guestRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(eq(1L), any(Pageable.class)))
                .thenReturn(visitPage);

        // Act
        PagedResponse<VisitResponse> result = visitService.getGuestVisits(1L, 0, 10, "visitDate", "DESC");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testVisit.getId(), result.getContent().get(0).getId());

        verify(guestRepository).existsById(1L);
        verify(visitRepository).findByGuestIdOrderByVisitDateDescVisitTimeDesc(eq(1L), any(Pageable.class));
    }

    @Test
    void getGuestVisits_GuestNotFound_ThrowsException() {
        // Arrange
        when(guestRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                visitService.getGuestVisits(1L, 0, 10, "visitDate", "DESC"));

        verify(guestRepository).existsById(1L);
        verify(visitRepository, never()).findByGuestIdOrderByVisitDateDescVisitTimeDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllGuestVisits_Success() {
        // Arrange
        when(guestRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(1L))
                .thenReturn(Arrays.asList(testVisit));

        // Act
        List<VisitResponse> result = visitService.getAllGuestVisits(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVisit.getId(), result.get(0).getId());

        verify(guestRepository).existsById(1L);
        verify(visitRepository).findByGuestIdOrderByVisitDateDescVisitTimeDesc(1L);
    }

    @Test
    void getVisitsByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Visit> visitPage = new PageImpl<>(Arrays.asList(testVisit), pageable, 1);
        
        when(visitRepository.findByVisitDateBetween(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(visitPage);

        // Act
        PagedResponse<VisitResponse> result = visitService.getVisitsByDateRange(startDate, endDate, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testVisit.getId(), result.getContent().get(0).getId());

        verify(visitRepository).findByVisitDateBetween(eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    void getTodaysVisits_Success() {
        // Arrange
        when(visitRepository.findTodaysVisits()).thenReturn(Arrays.asList(testVisit));

        // Act
        List<VisitResponse> result = visitService.getTodaysVisits();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVisit.getId(), result.get(0).getId());

        verify(visitRepository).findTodaysVisits();
    }

    @Test
    void deleteVisit_Success_Manager() {
        // Arrange
        when(authentication.getName()).thenReturn("manager@restaurant.com");
        when(staffRepository.findByEmail("manager@restaurant.com")).thenReturn(Optional.of(managerStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        visitService.deleteVisit(1L);

        // Assert
        verify(visitRepository).findById(1L);
        verify(auditLogService).logVisitDeleted(managerStaff.getId(), 1L, testGuest.getId());
        verify(visitRepository).delete(testVisit);
    }

    @Test
    void deleteVisit_AccessDenied_NonManager() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> visitService.deleteVisit(1L));

        verify(visitRepository).findById(1L);
        verify(visitRepository, never()).delete(any(Visit.class));
    }

    @Test
    void updateVisitNotes_Success_OriginalStaff() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));
        when(visitRepository.save(any(Visit.class))).thenReturn(testVisit);

        // Act
        VisitResponse result = visitService.updateVisitNotes(1L, "Updated notes");

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());

        verify(visitRepository).findById(1L);
        verify(visitRepository).save(any(Visit.class));
        verify(auditLogService).logVisitNotesUpdated(testStaff.getId(), 1L, testGuest.getId());
    }

    @Test
    void updateVisitNotes_AccessDenied_DifferentStaff() {
        // Arrange
        Staff differentStaff = new Staff();
        differentStaff.setId(3L);
        differentStaff.setRole(StaffRole.SERVER);
        
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(differentStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> visitService.updateVisitNotes(1L, "Updated notes"));

        verify(visitRepository).findById(1L);
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void getVisitNotes_Success() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        VisitNotesResponse result = visitService.getVisitNotes(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCanEdit()); // Original staff can edit

        verify(visitRepository).findById(1L);
    }

    @Test
    void getGuestVisitCount_Success() {
        // Arrange
        when(visitRepository.countByGuestId(1L)).thenReturn(5L);

        // Act
        long result = visitService.getGuestVisitCount(1L);

        // Assert
        assertEquals(5L, result);
        verify(visitRepository).countByGuestId(1L);
    }

    @Test
    void getLastVisitForGuest_Success() {
        // Arrange
        when(visitRepository.findLastVisitByGuestId(1L)).thenReturn(testVisit);

        // Act
        VisitResponse result = visitService.getLastVisitForGuest(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVisit.getId(), result.getId());

        verify(visitRepository).findLastVisitByGuestId(1L);
    }

    @Test
    void getLastVisitForGuest_NoVisits_ReturnsNull() {
        // Arrange
        when(visitRepository.findLastVisitByGuestId(1L)).thenReturn(null);

        // Act
        VisitResponse result = visitService.getLastVisitForGuest(1L);

        // Assert
        assertNull(result);
        verify(visitRepository).findLastVisitByGuestId(1L);
    }

    @Test
    void getGuestVisitHistory_Success() {
        // Arrange
        when(guestRepository.findById(1L)).thenReturn(Optional.of(testGuest));
        when(visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(1L))
                .thenReturn(Arrays.asList(testVisit));

        // Act
        VisitHistoryResponse result = visitService.getGuestVisitHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getGuestId());
        assertEquals("Alice Smith", result.getGuestName());
        assertEquals(1L, result.getTotalVisits());
        assertEquals(1, result.getVisits().size());

        verify(guestRepository).findById(1L);
        verify(visitRepository).findByGuestIdOrderByVisitDateDescVisitTimeDesc(1L);
    }

    @Test
    void getGuestVisitStatistics_Success() {
        // Arrange
        Object[] stats = {5L, LocalDate.now(), LocalDate.now().minusDays(30), 2.5};
        when(guestRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.getGuestVisitStatistics(1L)).thenReturn(stats);

        // Act
        Map<String, Object> result = visitService.getGuestVisitStatistics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.get("totalVisits"));
        assertEquals(LocalDate.now(), result.get("lastVisitDate"));
        assertEquals(LocalDate.now().minusDays(30), result.get("firstVisitDate"));
        assertEquals(2.5, result.get("averagePartySize"));

        verify(guestRepository).existsById(1L);
        verify(visitRepository).getGuestVisitStatistics(1L);
    }

    @Test
    void getRecentVisits_Success() {
        // Arrange
        when(visitRepository.findRecentVisits(any(LocalDate.class))).thenReturn(Arrays.asList(testVisit));

        // Act
        List<VisitResponse> result = visitService.getRecentVisits(7);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVisit.getId(), result.get(0).getId());

        verify(visitRepository).findRecentVisits(any(LocalDate.class));
    }

    @Test
    void searchVisitsByNotes_Success() {
        // Arrange
        when(visitRepository.findByServiceNotesContaining("great")).thenReturn(Arrays.asList(testVisit));

        // Act
        List<VisitResponse> result = visitService.searchVisitsByNotes("great");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testVisit.getId(), result.get(0).getId());

        verify(visitRepository).findByServiceNotesContaining("great");
    }

    @Test
    void canCurrentStaffEditVisitNotes_Success_OriginalStaff() {
        // Arrange
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        boolean result = visitService.canCurrentStaffEditVisitNotes(1L);

        // Assert
        assertTrue(result);

        verify(visitRepository).findById(1L);
    }

    @Test
    void canCurrentStaffEditVisitNotes_Success_Manager() {
        // Arrange
        when(authentication.getName()).thenReturn("manager@restaurant.com");
        when(staffRepository.findByEmail("manager@restaurant.com")).thenReturn(Optional.of(managerStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        boolean result = visitService.canCurrentStaffEditVisitNotes(1L);

        // Assert
        assertTrue(result);

        verify(visitRepository).findById(1L);
    }

    @Test
    void canCurrentStaffEditVisitNotes_False_DifferentStaff() {
        // Arrange
        Staff differentStaff = new Staff();
        differentStaff.setId(3L);
        differentStaff.setRole(StaffRole.SERVER);
        
        when(staffRepository.findByEmail("server@restaurant.com")).thenReturn(Optional.of(differentStaff));
        when(visitRepository.findById(1L)).thenReturn(Optional.of(testVisit));

        // Act
        boolean result = visitService.canCurrentStaffEditVisitNotes(1L);

        // Assert
        assertFalse(result);

        verify(visitRepository).findById(1L);
    }
}