package com.restaurant.vip.controller;

import com.restaurant.vip.audit.Auditable;
import com.restaurant.vip.audit.AuditAction;
import com.restaurant.vip.dto.*;
import com.restaurant.vip.service.DuplicateDetectionService;
import com.restaurant.vip.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/guests")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GuestController {
    
    private final GuestService guestService;
    
    @Autowired
    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }
    
    /**
     * Create a new guest
     * POST /api/guests
     */
    @PostMapping
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.CREATE, tableName = "guests", description = "Create new guest profile", logParameters = true, logReturnValue = true)
    public ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody GuestCreateRequest request) {
        GuestResponse response = guestService.createGuest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get guest by ID
     * GET /api/guests/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Access guest profile")
    public ResponseEntity<GuestResponse> getGuestById(@PathVariable Long id) {
        GuestResponse response = guestService.getGuestById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update guest
     * PUT /api/guests/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.UPDATE, tableName = "guests", description = "Update guest profile", logParameters = true, sensitive = true)
    public ResponseEntity<GuestResponse> updateGuest(
            @PathVariable Long id, 
            @Valid @RequestBody GuestUpdateRequest request) {
        GuestResponse response = guestService.updateGuest(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Soft delete guest
     * DELETE /api/guests/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Auditable(action = AuditAction.DELETE, tableName = "guests", description = "Delete guest profile", sensitive = true)
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all guests with pagination
     * GET /api/guests?page=0&size=20&sortBy=firstName&sortDirection=ASC
     */
    @GetMapping
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Access all guest profiles")
    public ResponseEntity<PagedResponse<GuestResponse>> getAllGuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        PagedResponse<GuestResponse> response = guestService.getAllGuests(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search guests with advanced filters
     * POST /api/guests/search
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Search guest profiles", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> searchGuests(
            @RequestBody GuestSearchRequest searchRequest) {
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Quick search by name or phone (GET version for simple searches)
     * GET /api/guests/search?q=searchTerm&page=0&size=20
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Quick search guest profiles", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> quickSearch(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setSearchTerm(q);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if phone number exists (for duplicate detection)
     * GET /api/guests/check-phone?phone=+1234567890
     */
    @GetMapping("/check-phone")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Check phone number existence", logParameters = true)
    public ResponseEntity<DuplicateCheckResponse> checkPhoneExists(@RequestParam String phone) {
        boolean exists = guestService.existsByPhone(phone);
        
        DuplicateCheckResponse response = new DuplicateCheckResponse();
        response.setExists(exists);
        response.setPhone(phone);
        
        if (exists) {
            Optional<GuestResponse> existingGuest = guestService.findByPhone(phone);
            if (existingGuest.isPresent()) {
                response.setExistingGuest(existingGuest.get());
                response.setMessage("A guest with this phone number already exists: " + 
                                  existingGuest.get().getFullName());
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get guests with upcoming special occasions
     * GET /api/guests/upcoming-occasions
     */
    @GetMapping("/upcoming-occasions")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Access guests with upcoming occasions")
    public ResponseEntity<PagedResponse<GuestResponse>> getGuestsWithUpcomingOccasions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setUpcomingOccasions(true);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy("firstName");
        searchRequest.setSortDirection("ASC");
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Filter guests by dietary restrictions
     * GET /api/guests/filter/dietary?restrictions=Vegetarian,Vegan&page=0&size=20
     */
    @GetMapping("/filter/dietary")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Filter guests by dietary restrictions", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> filterByDietaryRestrictions(
            @RequestParam List<String> restrictions,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setDietaryRestrictions(restrictions);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Filter guests by favorite drinks
     * GET /api/guests/filter/drinks?drinks=Wine,Beer&page=0&size=20
     */
    @GetMapping("/filter/drinks")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Filter guests by favorite drinks", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> filterByFavoriteDrinks(
            @RequestParam List<String> drinks,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setFavoriteDrinks(drinks);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Filter guests by seating preference
     * GET /api/guests/filter/seating?preference=Window&page=0&size=20
     */
    @GetMapping("/filter/seating")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Filter guests by seating preference", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> filterBySeatingPreference(
            @RequestParam String preference,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setSeatingPreference(preference);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get guests with birthdays or anniversaries
     * GET /api/guests/filter/occasions?hasBirthday=true&hasAnniversary=false
     */
    @GetMapping("/filter/occasions")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Filter guests by occasions", logParameters = true)
    public ResponseEntity<PagedResponse<GuestResponse>> filterByOccasions(
            @RequestParam(required = false) Boolean hasBirthday,
            @RequestParam(required = false) Boolean hasAnniversary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        GuestSearchRequest searchRequest = new GuestSearchRequest();
        searchRequest.setHasBirthday(hasBirthday);
        searchRequest.setHasAnniversary(hasAnniversary);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        PagedResponse<GuestResponse> response = guestService.searchGuests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Upload photo for guest
     * POST /api/guests/{id}/photo
     */
    @PostMapping("/{id}/photo")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.UPDATE, tableName = "guests", description = "Upload guest photo", sensitive = true)
    public ResponseEntity<GuestResponse> uploadGuestPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        GuestResponse response = guestService.uploadGuestPhoto(id, file);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete photo for guest
     * DELETE /api/guests/{id}/photo
     */
    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.DELETE, tableName = "guests", description = "Delete guest photo", sensitive = true)
    public ResponseEntity<GuestResponse> deleteGuestPhoto(@PathVariable Long id) {
        GuestResponse response = guestService.deleteGuestPhoto(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check for potential duplicates before creating guest
     * POST /api/guests/check-duplicates
     */
    @PostMapping("/check-duplicates")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Check potential duplicates", logParameters = true)
    public ResponseEntity<PotentialDuplicatesResponse> checkPotentialDuplicates(
            @Valid @RequestBody GuestCreateRequest request) {
        PotentialDuplicatesResponse response = guestService.findPotentialDuplicates(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check for potential duplicates before updating guest
     * POST /api/guests/{id}/check-duplicates
     */
    @PostMapping("/{id}/check-duplicates")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Check potential duplicates for update", logParameters = true)
    public ResponseEntity<PotentialDuplicatesResponse> checkPotentialDuplicatesForUpdate(
            @PathVariable Long id,
            @Valid @RequestBody GuestUpdateRequest request) {
        PotentialDuplicatesResponse response = guestService.findPotentialDuplicatesForUpdate(request, id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Comprehensive duplicate check (exact + potential)
     * POST /api/guests/comprehensive-duplicate-check
     */
    @PostMapping("/comprehensive-duplicate-check")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Comprehensive duplicate check", logParameters = true)
    public ResponseEntity<DuplicateCheckResponse> comprehensiveDuplicateCheck(
            @Valid @RequestBody GuestCreateRequest request) {
        DuplicateCheckResponse response = guestService.comprehensiveDuplicateCheck(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enhanced phone check with duplicate validation for updates
     * GET /api/guests/{id}/check-phone?phone=+1234567890
     */
    @GetMapping("/{id}/check-phone")
    @PreAuthorize("hasRole('HOST') or hasRole('SERVER') or hasRole('MANAGER')")
    @Auditable(action = AuditAction.READ, tableName = "guests", description = "Check phone existence for update", logParameters = true)
    public ResponseEntity<DuplicateCheckResponse> checkPhoneExistsForUpdate(
            @PathVariable Long id,
            @RequestParam String phone) {
        DuplicateCheckResponse response = guestService.checkDuplicateByPhoneForUpdate(phone, id);
        return ResponseEntity.ok(response);
    }
}