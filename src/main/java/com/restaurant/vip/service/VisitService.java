package com.restaurant.vip.service;

import com.restaurant.vip.dto.PagedResponse;
import com.restaurant.vip.dto.VisitCreateRequest;
import com.restaurant.vip.dto.VisitHistoryResponse;
import com.restaurant.vip.dto.VisitNotesResponse;
import com.restaurant.vip.dto.VisitResponse;
import com.restaurant.vip.dto.VisitUpdateRequest;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.exception.ResourceNotFoundException;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import com.restaurant.vip.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VisitService {
    
    private final VisitRepository visitRepository;
    private final GuestRepository guestRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;
    
    @Autowired
    public VisitService(VisitRepository visitRepository, 
                       GuestRepository guestRepository,
                       StaffRepository staffRepository,
                       AuditLogService auditLogService) {
        this.visitRepository = visitRepository;
        this.guestRepository = guestRepository;
        this.staffRepository = staffRepository;
        this.auditLogService = auditLogService;
    }
    
    /**
     * Create a new visit record
     */
    public VisitResponse createVisit(VisitCreateRequest request) {
        // Get current authenticated staff member
        Staff currentStaff = getCurrentStaff();
        
        // Validate guest exists
        Guest guest = guestRepository.findById(request.getGuestId())
            .orElseThrow(() -> new ResourceNotFoundException("Guest not found with ID: " + request.getGuestId()));
        
        // Create visit entity
        Visit visit = new Visit();
        visit.setGuest(guest);
        visit.setStaff(currentStaff);
        visit.setVisitDate(request.getVisitDate());
        visit.setVisitTime(request.getVisitTime());
        visit.setPartySize(request.getPartySize());
        visit.setTableNumber(request.getTableNumber());
        visit.setServiceNotes(request.getServiceNotes());
        
        // Save visit
        Visit savedVisit = visitRepository.save(visit);
        
        // Log audit event
        auditLogService.logVisitCreated(currentStaff.getId(), savedVisit.getId(), guest.getId());
        
        return new VisitResponse(savedVisit);
    }
    
    /**
     * Update an existing visit record
     */
    public VisitResponse updateVisit(Long visitId, VisitUpdateRequest request) {
        // Get current authenticated staff member
        Staff currentStaff = getCurrentStaff();
        
        // Find existing visit
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Check permissions - only original staff member or managers can edit
        if (!canEditVisit(currentStaff, visit)) {
            throw new AccessDeniedException("You don't have permission to edit this visit");
        }
        
        // Update visit fields
        visit.setVisitDate(request.getVisitDate());
        visit.setVisitTime(request.getVisitTime());
        visit.setPartySize(request.getPartySize());
        visit.setTableNumber(request.getTableNumber());
        visit.setServiceNotes(request.getServiceNotes());
        
        // Save updated visit
        Visit updatedVisit = visitRepository.save(visit);
        
        // Log audit event
        auditLogService.logVisitUpdated(currentStaff.getId(), visitId, visit.getGuest().getId());
        
        return new VisitResponse(updatedVisit);
    }
    
    /**
     * Get visit by ID
     */
    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        return new VisitResponse(visit);
    }
    
    /**
     * Get all visits for a guest with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<VisitResponse> getGuestVisits(Long guestId, int page, int size, String sortBy, String sortDir) {
        // Validate guest exists
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with ID: " + guestId);
        }
        
        // Create sort object
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        if (!"visitDate".equals(sortBy) && !"visitTime".equals(sortBy) && !"createdAt".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "visitDate", "visitTime");
        }
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get visits
        Page<Visit> visitPage = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(guestId, pageable);
        
        // Convert to response DTOs
        List<VisitResponse> visitResponses = visitPage.getContent().stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
        
        return new PagedResponse<VisitResponse>(
            visitResponses,
            visitPage.getNumber(),
            visitPage.getSize(),
            visitPage.getTotalElements(),
            visitPage.getTotalPages()
        );
    }
    
    /**
     * Get all visits for a guest (non-paginated)
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getAllGuestVisits(Long guestId) {
        // Validate guest exists
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with ID: " + guestId);
        }
        
        List<Visit> visits = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(guestId);
        
        return visits.stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Get visits by date range
     */
    @Transactional(readOnly = true)
    public PagedResponse<VisitResponse> getVisitsByDateRange(LocalDate startDate, LocalDate endDate, 
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "visitDate", "visitTime"));
        
        Page<Visit> visitPage = visitRepository.findByVisitDateBetween(startDate, endDate, pageable);
        
        List<VisitResponse> visitResponses = visitPage.getContent().stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
        
        return new PagedResponse<VisitResponse>(
            visitResponses,
            visitPage.getNumber(),
            visitPage.getSize(),
            visitPage.getTotalElements(),
            visitPage.getTotalPages()
        );
    }
    
    /**
     * Get today's visits
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getTodaysVisits() {
        List<Visit> visits = visitRepository.findTodaysVisits();
        
        return visits.stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Delete a visit (soft delete by setting notes to indicate deletion)
     */
    public void deleteVisit(Long visitId) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Only managers can delete visits
        if (!currentStaff.getRole().equals(StaffRole.MANAGER)) {
            throw new AccessDeniedException("Only managers can delete visits");
        }
        
        // Log audit event before deletion
        auditLogService.logVisitDeleted(currentStaff.getId(), visitId, visit.getGuest().getId());
        
        // Delete the visit
        visitRepository.delete(visit);
    }
    
    /**
     * Update visit notes only
     */
    public VisitResponse updateVisitNotes(Long visitId, String notes) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Check permissions for note editing
        if (!canEditVisitNotes(currentStaff, visit)) {
            throw new AccessDeniedException("You don't have permission to edit notes for this visit");
        }
        
        visit.setServiceNotes(notes);
        Visit updatedVisit = visitRepository.save(visit);
        
        // Log audit event
        auditLogService.logVisitNotesUpdated(currentStaff.getId(), visitId, visit.getGuest().getId());
        
        return new VisitResponse(updatedVisit);
    }
    
    /**
     * Get visit notes with permission information
     */
    @Transactional(readOnly = true)
    public VisitNotesResponse getVisitNotes(Long visitId) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        boolean canEdit = canEditVisitNotes(currentStaff, visit);
        
        return new VisitNotesResponse(visit, canEdit);
    }
    
    /**
     * Add notes to a visit (for visits without existing notes)
     */
    public VisitNotesResponse addVisitNotes(Long visitId, String notes) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Check if visit already has notes
        if (visit.getServiceNotes() != null && !visit.getServiceNotes().trim().isEmpty()) {
            throw new IllegalStateException("Visit already has notes. Use update endpoint instead.");
        }
        
        // Check permissions
        if (!canEditVisitNotes(currentStaff, visit)) {
            throw new AccessDeniedException("You don't have permission to add notes to this visit");
        }
        
        visit.setServiceNotes(notes);
        Visit updatedVisit = visitRepository.save(visit);
        
        // Log audit event
        auditLogService.logVisitNotesUpdated(currentStaff.getId(), visitId, visit.getGuest().getId());
        
        boolean canEdit = canEditVisitNotes(currentStaff, updatedVisit);
        return new VisitNotesResponse(updatedVisit, canEdit);
    }
    
    /**
     * Update existing visit notes
     */
    public VisitNotesResponse updateVisitNotesEnhanced(Long visitId, String notes) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Check permissions for note editing
        if (!canEditVisitNotes(currentStaff, visit)) {
            throw new AccessDeniedException("You don't have permission to edit notes for this visit");
        }
        
        String oldNotes = visit.getServiceNotes();
        visit.setServiceNotes(notes);
        Visit updatedVisit = visitRepository.save(visit);
        
        // Log audit event with old and new values
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("serviceNotes", oldNotes);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("serviceNotes", notes);
        
        auditLogService.logDataModification(currentStaff, "visits", visitId, 
                                          "VISIT_NOTES_UPDATED", oldValues, newValues);
        
        boolean canEdit = canEditVisitNotes(currentStaff, updatedVisit);
        return new VisitNotesResponse(updatedVisit, canEdit);
    }
    
    /**
     * Clear visit notes (set to empty)
     */
    public VisitNotesResponse clearVisitNotes(Long visitId) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        // Only managers can clear notes
        if (!currentStaff.getRole().equals(StaffRole.MANAGER)) {
            throw new AccessDeniedException("Only managers can clear visit notes");
        }
        
        String oldNotes = visit.getServiceNotes();
        visit.setServiceNotes(null);
        Visit updatedVisit = visitRepository.save(visit);
        
        // Log audit event
        Map<String, Object> oldValues = new HashMap<>();
        oldValues.put("serviceNotes", oldNotes);
        
        Map<String, Object> newValues = new HashMap<>();
        newValues.put("serviceNotes", null);
        
        auditLogService.logDataModification(currentStaff, "visits", visitId, 
                                          "VISIT_NOTES_CLEARED", oldValues, newValues);
        
        boolean canEdit = canEditVisitNotes(currentStaff, updatedVisit);
        return new VisitNotesResponse(updatedVisit, canEdit);
    }
    
    /**
     * Get all visits with notes for a guest
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getGuestVisitsWithNotes(Long guestId) {
        // Validate guest exists
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with ID: " + guestId);
        }
        
        List<Visit> visits = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(guestId);
        
        return visits.stream()
            .filter(visit -> visit.getServiceNotes() != null && !visit.getServiceNotes().trim().isEmpty())
            .map(VisitResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if current staff can edit notes for a specific visit
     */
    @Transactional(readOnly = true)
    public boolean canCurrentStaffEditVisitNotes(Long visitId) {
        Staff currentStaff = getCurrentStaff();
        
        Visit visit = visitRepository.findById(visitId)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found with ID: " + visitId));
        
        return canEditVisitNotes(currentStaff, visit);
    }
    
    /**
     * Get visit count for a guest
     */
    @Transactional(readOnly = true)
    public long getGuestVisitCount(Long guestId) {
        return visitRepository.countByGuestId(guestId);
    }
    
    /**
     * Get last visit for a guest
     */
    @Transactional(readOnly = true)
    public VisitResponse getLastVisitForGuest(Long guestId) {
        Visit lastVisit = visitRepository.findLastVisitByGuestId(guestId);
        if (lastVisit == null) {
            return null;
        }
        return new VisitResponse(lastVisit);
    }
    
    /**
     * Get comprehensive visit history for a guest (timeline format)
     */
    @Transactional(readOnly = true)
    public VisitHistoryResponse getGuestVisitHistory(Long guestId) {
        // Validate guest exists
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new ResourceNotFoundException("Guest not found with ID: " + guestId));
        
        // Get all visits for the guest
        List<Visit> visits = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(guestId);
        
        // Convert to visit summaries
        List<VisitHistoryResponse.VisitSummary> visitSummaries = visits.stream()
            .map(VisitHistoryResponse.VisitSummary::new)
            .collect(Collectors.toList());
        
        // Calculate statistics
        long totalVisits = visits.size();
        LocalDate firstVisitDate = visits.isEmpty() ? null : 
            visits.get(visits.size() - 1).getVisitDate();
        LocalDate lastVisitDate = visits.isEmpty() ? null : 
            visits.get(0).getVisitDate();
        
        String guestName = guest.getFirstName() + 
                          (guest.getLastName() != null ? " " + guest.getLastName() : "");
        
        return new VisitHistoryResponse(guestId, guestName, totalVisits, 
                                       firstVisitDate, lastVisitDate, visitSummaries);
    }
    
    /**
     * Get paginated visit history for a guest (timeline format)
     */
    @Transactional(readOnly = true)
    public PagedResponse<VisitHistoryResponse.VisitSummary> getGuestVisitHistoryPaginated(
            Long guestId, int page, int size, String sortBy, String sortDir) {
        
        // Validate guest exists
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with ID: " + guestId);
        }
        
        // Create sort object
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        if (!"visitDate".equals(sortBy) && !"visitTime".equals(sortBy) && !"createdAt".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "visitDate", "visitTime");
        }
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get visits
        Page<Visit> visitPage = visitRepository.findByGuestIdOrderByVisitDateDescVisitTimeDesc(guestId, pageable);
        
        // Convert to visit summaries
        List<VisitHistoryResponse.VisitSummary> visitSummaries = visitPage.getContent().stream()
            .map(VisitHistoryResponse.VisitSummary::new)
            .collect(Collectors.toList());
        
        return new PagedResponse<VisitHistoryResponse.VisitSummary>(
            visitSummaries,
            visitPage.getNumber(),
            visitPage.getSize(),
            visitPage.getTotalElements(),
            visitPage.getTotalPages()
        );
    }
    
    /**
     * Get visit statistics for a guest
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGuestVisitStatistics(Long guestId) {
        // Validate guest exists
        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found with ID: " + guestId);
        }
        
        Object[] stats = visitRepository.getGuestVisitStatistics(guestId);
        
        Map<String, Object> statistics = new HashMap<>();
        if (stats != null && stats.length > 0) {
            statistics.put("totalVisits", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
            statistics.put("lastVisitDate", stats[1]);
            statistics.put("firstVisitDate", stats[2]);
            statistics.put("averagePartySize", stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0);
        } else {
            statistics.put("totalVisits", 0L);
            statistics.put("lastVisitDate", null);
            statistics.put("firstVisitDate", null);
            statistics.put("averagePartySize", 0.0);
        }
        
        return statistics;
    }
    
    /**
     * Get recent visits (last N days)
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> getRecentVisits(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        List<Visit> visits = visitRepository.findRecentVisits(cutoffDate);
        
        return visits.stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Search visits by service notes
     */
    @Transactional(readOnly = true)
    public List<VisitResponse> searchVisitsByNotes(String searchTerm) {
        List<Visit> visits = visitRepository.findByServiceNotesContaining(searchTerm);
        
        return visits.stream()
            .map(VisitResponse::new)
            .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private Staff getCurrentStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        return staffRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
    }
    
    private boolean canEditVisit(Staff staff, Visit visit) {
        // Managers can edit any visit
        if (staff.getRole().equals(StaffRole.MANAGER)) {
            return true;
        }
        
        // Original staff member can edit their own visits
        return visit.getStaff().getId().equals(staff.getId());
    }
    
    private boolean canEditVisitNotes(Staff staff, Visit visit) {
        // Managers can edit any visit notes
        if (staff.getRole().equals(StaffRole.MANAGER)) {
            return true;
        }
        
        // Original staff member can edit their own visit notes
        return visit.getStaff().getId().equals(staff.getId());
    }
}