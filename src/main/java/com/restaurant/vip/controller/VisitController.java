package com.restaurant.vip.controller;

import com.restaurant.vip.audit.Auditable;
import com.restaurant.vip.audit.AuditAction;
import com.restaurant.vip.dto.PagedResponse;
import com.restaurant.vip.dto.VisitCreateRequest;
import com.restaurant.vip.dto.VisitHistoryResponse;
import com.restaurant.vip.dto.VisitNotesRequest;
import com.restaurant.vip.dto.VisitNotesResponse;
import com.restaurant.vip.dto.VisitResponse;
import com.restaurant.vip.dto.VisitUpdateRequest;
import com.restaurant.vip.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {
    
    private final VisitService visitService;
    
    @Autowired
    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }
    
    /**
     * Create a new visit record
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.CREATE, tableName = "visits", description = "Create new visit record", logParameters = true, logReturnValue = true)
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody VisitCreateRequest request) {
        VisitResponse response = visitService.createVisit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get visit by ID
     */
    @GetMapping("/{visitId}")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "visits", description = "Access visit record")
    public ResponseEntity<VisitResponse> getVisit(@PathVariable Long visitId) {
        VisitResponse response = visitService.getVisitById(visitId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing visit record
     */
    @PutMapping("/{visitId}")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.UPDATE, tableName = "visits", description = "Update visit record", logParameters = true, sensitive = true)
    public ResponseEntity<VisitResponse> updateVisit(@PathVariable Long visitId, 
                                                   @Valid @RequestBody VisitUpdateRequest request) {
        VisitResponse response = visitService.updateVisit(visitId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a visit record (managers only)
     */
    @DeleteMapping("/{visitId}")
    @PreAuthorize("hasRole('MANAGER')")
    @Auditable(action = AuditAction.DELETE, tableName = "visits", description = "Delete visit record", sensitive = true)
    public ResponseEntity<Void> deleteVisit(@PathVariable Long visitId) {
        visitService.deleteVisit(visitId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update visit notes only (legacy endpoint)
     */
    @PatchMapping("/{visitId}/notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.UPDATE, tableName = "visits", description = "Update visit notes", logParameters = true, sensitive = true)
    public ResponseEntity<VisitResponse> updateVisitNotes(@PathVariable Long visitId, 
                                                        @RequestBody Map<String, String> request) {
        String notes = request.get("notes");
        VisitResponse response = visitService.updateVisitNotes(visitId, notes);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get visit notes with permission information
     */
    @GetMapping("/{visitId}/notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "visits", description = "Access visit notes")
    public ResponseEntity<VisitNotesResponse> getVisitNotes(@PathVariable Long visitId) {
        VisitNotesResponse response = visitService.getVisitNotes(visitId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Add notes to a visit (for visits without existing notes)
     */
    @PostMapping("/{visitId}/notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.CREATE, tableName = "visits", description = "Add visit notes", logParameters = true, sensitive = true)
    public ResponseEntity<VisitNotesResponse> addVisitNotes(@PathVariable Long visitId, 
                                                          @Valid @RequestBody VisitNotesRequest request) {
        VisitNotesResponse response = visitService.addVisitNotes(visitId, request.getNotes());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Update existing visit notes (enhanced version)
     */
    @PutMapping("/{visitId}/notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    @Auditable(action = AuditAction.UPDATE, tableName = "visits", description = "Update visit notes enhanced", logParameters = true, sensitive = true)
    public ResponseEntity<VisitNotesResponse> updateVisitNotesEnhanced(@PathVariable Long visitId, 
                                                                     @Valid @RequestBody VisitNotesRequest request) {
        VisitNotesResponse response = visitService.updateVisitNotesEnhanced(visitId, request.getNotes());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear visit notes (managers only)
     */
    @DeleteMapping("/{visitId}/notes")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VisitNotesResponse> clearVisitNotes(@PathVariable Long visitId) {
        VisitNotesResponse response = visitService.clearVisitNotes(visitId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all visits with notes for a guest
     */
    @GetMapping("/guest/{guestId}/with-notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<VisitResponse>> getGuestVisitsWithNotes(@PathVariable Long guestId) {
        List<VisitResponse> response = visitService.getGuestVisitsWithNotes(guestId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if current staff can edit notes for a specific visit
     */
    @GetMapping("/{visitId}/notes/can-edit")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<Map<String, Boolean>> canEditVisitNotes(@PathVariable Long visitId) {
        boolean canEdit = visitService.canCurrentStaffEditVisitNotes(visitId);
        return ResponseEntity.ok(Map.of("canEdit", canEdit));
    }
    
    /**
     * Get all visits for a specific guest with pagination
     */
    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<PagedResponse<VisitResponse>> getGuestVisits(
            @PathVariable Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        PagedResponse<VisitResponse> response = visitService.getGuestVisits(guestId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all visits for a specific guest (non-paginated)
     */
    @GetMapping("/guest/{guestId}/all")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<VisitResponse>> getAllGuestVisits(@PathVariable Long guestId) {
        List<VisitResponse> response = visitService.getAllGuestVisits(guestId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get visits by date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<PagedResponse<VisitResponse>> getVisitsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PagedResponse<VisitResponse> response = visitService.getVisitsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get today's visits
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<VisitResponse>> getTodaysVisits() {
        List<VisitResponse> response = visitService.getTodaysVisits();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get visit count for a guest
     */
    @GetMapping("/guest/{guestId}/count")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<Map<String, Long>> getGuestVisitCount(@PathVariable Long guestId) {
        long count = visitService.getGuestVisitCount(guestId);
        return ResponseEntity.ok(Map.of("visitCount", count));
    }
    
    /**
     * Get last visit for a guest
     */
    @GetMapping("/guest/{guestId}/last")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<VisitResponse> getLastVisitForGuest(@PathVariable Long guestId) {
        VisitResponse response = visitService.getLastVisitForGuest(guestId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get comprehensive visit history for a guest (timeline format)
     */
    @GetMapping("/guest/{guestId}/history")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<VisitHistoryResponse> getGuestVisitHistory(@PathVariable Long guestId) {
        VisitHistoryResponse response = visitService.getGuestVisitHistory(guestId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get paginated visit history for a guest (timeline format)
     */
    @GetMapping("/guest/{guestId}/history/paginated")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<PagedResponse<VisitHistoryResponse.VisitSummary>> getGuestVisitHistoryPaginated(
            @PathVariable Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        PagedResponse<VisitHistoryResponse.VisitSummary> response = 
            visitService.getGuestVisitHistoryPaginated(guestId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get visit statistics for a guest
     */
    @GetMapping("/guest/{guestId}/statistics")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getGuestVisitStatistics(@PathVariable Long guestId) {
        Map<String, Object> statistics = visitService.getGuestVisitStatistics(guestId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get recent visits (last N days)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<VisitResponse>> getRecentVisits(
            @RequestParam(defaultValue = "7") int days) {
        List<VisitResponse> response = visitService.getRecentVisits(days);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search visits by service notes
     */
    @GetMapping("/search/notes")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<VisitResponse>> searchVisitsByNotes(
            @RequestParam String searchTerm) {
        List<VisitResponse> response = visitService.searchVisitsByNotes(searchTerm);
        return ResponseEntity.ok(response);
    }
}