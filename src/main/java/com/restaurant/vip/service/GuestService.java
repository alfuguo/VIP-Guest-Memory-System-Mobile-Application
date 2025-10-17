package com.restaurant.vip.service;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.service.FileUploadService;
import com.restaurant.vip.service.DuplicateDetectionService;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.exception.DuplicateGuestException;
import com.restaurant.vip.exception.ResourceNotFoundException;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuestService {
    
    private final GuestRepository guestRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;
    private final FileUploadService fileUploadService;
    private final DuplicateDetectionService duplicateDetectionService;
    
    @Autowired
    public GuestService(GuestRepository guestRepository, 
                       StaffRepository staffRepository,
                       AuditLogService auditLogService,
                       FileUploadService fileUploadService,
                       DuplicateDetectionService duplicateDetectionService) {
        this.guestRepository = guestRepository;
        this.staffRepository = staffRepository;
        this.auditLogService = auditLogService;
        this.fileUploadService = fileUploadService;
        this.duplicateDetectionService = duplicateDetectionService;
    }
    
    /**
     * Create a new guest
     */
    public GuestResponse createGuest(GuestCreateRequest request) {
        // Check for duplicate phone number
        if (guestRepository.existsByPhone(request.getPhone())) {
            Optional<Guest> existingGuest = guestRepository.findByPhone(request.getPhone());
            if (existingGuest.isPresent()) {
                throw new DuplicateGuestException(request.getPhone(), existingGuest.get().getId());
            }
        }
        
        // Get current authenticated staff
        Staff currentStaff = getCurrentStaff();
        
        // Create new guest entity
        Guest guest = new Guest();
        mapRequestToEntity(request, guest);
        guest.setCreatedBy(currentStaff);
        
        // Save guest
        Guest savedGuest = guestRepository.save(guest);
        
        // Log the action
        auditLogService.logAction("CREATE_GUEST", "Guest created: " + savedGuest.getFullName(), 
                                 savedGuest.getId(), currentStaff.getId());
        
        return mapEntityToResponse(savedGuest);
    }
    
    /**
     * Get guest by ID
     */
    @Transactional(readOnly = true)
    public GuestResponse getGuestById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", id));
        
        // Log the access
        Staff currentStaff = getCurrentStaff();
        auditLogService.logAction("VIEW_GUEST", "Guest profile viewed: " + guest.getFullName(), 
                                 guest.getId(), currentStaff.getId());
        
        return mapEntityToResponse(guest);
    }
    
    /**
     * Update guest
     */
    public GuestResponse updateGuest(Long id, GuestUpdateRequest request) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", id));
        
        // Check for duplicate phone number (excluding current guest)
        if (!guest.getPhone().equals(request.getPhone()) && 
            guestRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            Optional<Guest> existingGuest = guestRepository.findByPhone(request.getPhone());
            if (existingGuest.isPresent()) {
                throw new DuplicateGuestException(request.getPhone(), existingGuest.get().getId());
            }
        }
        
        // Update guest entity
        mapRequestToEntity(request, guest);
        
        // Save updated guest
        Guest updatedGuest = guestRepository.save(guest);
        
        // Log the action
        Staff currentStaff = getCurrentStaff();
        auditLogService.logAction("UPDATE_GUEST", "Guest updated: " + updatedGuest.getFullName(), 
                                 updatedGuest.getId(), currentStaff.getId());
        
        return mapEntityToResponse(updatedGuest);
    }
    
    /**
     * Soft delete guest
     */
    public void deleteGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", id));
        
        // Perform soft delete
        guest.softDelete();
        guestRepository.save(guest);
        
        // Log the action
        Staff currentStaff = getCurrentStaff();
        auditLogService.logAction("DELETE_GUEST", "Guest deleted: " + guest.getFullName(), 
                                 guest.getId(), currentStaff.getId());
    }
    
    /**
     * Search guests with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<GuestResponse> searchGuests(GuestSearchRequest searchRequest) {
        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), 
                           searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        
        Page<Guest> guestPage;
        
        // Handle different search scenarios
        if (searchRequest.getUpcomingOccasions() != null && searchRequest.getUpcomingOccasions()) {
            // Search for guests with upcoming occasions
            List<Guest> upcomingGuests = guestRepository.findGuestsWithUpcomingOccasions();
            guestPage = convertListToPage(upcomingGuests, pageable);
        } else if (hasComplexFilters(searchRequest)) {
            // Use complex search with all filters including dietary restrictions and drinks
            guestPage = guestRepository.complexSearch(
                searchRequest.getSearchTerm(),
                searchRequest.getSeatingPreference(),
                searchRequest.getHasBirthday(),
                searchRequest.getHasAnniversary(),
                searchRequest.getDietaryRestrictions(),
                searchRequest.getFavoriteDrinks(),
                pageable
            );
        } else if (hasAdvancedFilters(searchRequest)) {
            // Use advanced search with basic filters
            guestPage = guestRepository.advancedSearch(
                searchRequest.getSearchTerm(),
                searchRequest.getSeatingPreference(),
                searchRequest.getHasBirthday(),
                searchRequest.getHasAnniversary(),
                pageable
            );
        } else if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            // Simple search by name or phone
            guestPage = guestRepository.searchByNameOrPhone(searchRequest.getSearchTerm().trim(), pageable);
        } else {
            // Get all guests with pagination
            guestPage = guestRepository.findAll(pageable);
        }
        
        // Convert to response DTOs
        List<GuestResponse> guestResponses = guestPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            guestResponses,
            guestPage.getNumber(),
            guestPage.getSize(),
            guestPage.getTotalElements(),
            guestPage.getTotalPages()
        );
    }
    
    /**
     * Get all guests (for simple listing)
     */
    @Transactional(readOnly = true)
    public PagedResponse<GuestResponse> getAllGuests(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Guest> guestPage = guestRepository.findAll(pageable);
        
        List<GuestResponse> guestResponses = guestPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            guestResponses,
            guestPage.getNumber(),
            guestPage.getSize(),
            guestPage.getTotalElements(),
            guestPage.getTotalPages()
        );
    }
    
    /**
     * Check if guest exists by phone (for duplicate detection)
     */
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return guestRepository.existsByPhone(phone);
    }
    
    /**
     * Find guest by phone
     */
    @Transactional(readOnly = true)
    public Optional<GuestResponse> findByPhone(String phone) {
        return guestRepository.findByPhone(phone)
                .map(this::mapEntityToResponse);
    }
    
    /**
     * Upload photo for guest
     */
    public GuestResponse uploadGuestPhoto(Long guestId, MultipartFile file) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));
        
        try {
            // Delete old photo if exists
            if (guest.getPhotoUrl() != null && !guest.getPhotoUrl().isEmpty()) {
                deleteOldPhoto(guest.getPhotoUrl());
            }
            
            // Upload new photo
            FileUploadResponse uploadResponse = fileUploadService.uploadGuestPhoto(file, guestId);
            
            // Update guest with new photo URL
            guest.setPhotoUrl(uploadResponse.getUrl());
            Guest updatedGuest = guestRepository.save(guest);
            
            // Log the action
            Staff currentStaff = getCurrentStaff();
            auditLogService.logAction("UPLOAD_PHOTO", "Photo uploaded for guest: " + guest.getFullName(), 
                                     guest.getId(), currentStaff.getId());
            
            return mapEntityToResponse(updatedGuest);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete photo for guest
     */
    public GuestResponse deleteGuestPhoto(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", "id", guestId));
        
        // Delete photo file if exists
        if (guest.getPhotoUrl() != null && !guest.getPhotoUrl().isEmpty()) {
            deleteOldPhoto(guest.getPhotoUrl());
        }
        
        // Remove photo URL from guest
        guest.setPhotoUrl(null);
        Guest updatedGuest = guestRepository.save(guest);
        
        // Log the action
        Staff currentStaff = getCurrentStaff();
        auditLogService.logAction("DELETE_PHOTO", "Photo deleted for guest: " + guest.getFullName(), 
                                 guest.getId(), currentStaff.getId());
        
        return mapEntityToResponse(updatedGuest);
    }
    
    /**
     * Check for exact duplicate by phone
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkDuplicateByPhone(String phone) {
        return duplicateDetectionService.checkExactDuplicate(phone);
    }
    
    /**
     * Check for exact duplicate by phone for update
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse checkDuplicateByPhoneForUpdate(String phone, Long guestId) {
        return duplicateDetectionService.checkExactDuplicateForUpdate(phone, guestId);
    }
    
    /**
     * Find potential duplicates for new guest
     */
    @Transactional(readOnly = true)
    public PotentialDuplicatesResponse findPotentialDuplicates(GuestCreateRequest request) {
        List<GuestResponse> potentialDuplicates = duplicateDetectionService.findPotentialDuplicates(request);
        return new PotentialDuplicatesResponse(potentialDuplicates);
    }
    
    /**
     * Find potential duplicates for guest update
     */
    @Transactional(readOnly = true)
    public PotentialDuplicatesResponse findPotentialDuplicatesForUpdate(GuestUpdateRequest request, Long guestId) {
        List<GuestResponse> potentialDuplicates = duplicateDetectionService.findPotentialDuplicatesForUpdate(request, guestId);
        return new PotentialDuplicatesResponse(potentialDuplicates);
    }
    
    /**
     * Comprehensive duplicate check
     */
    @Transactional(readOnly = true)
    public DuplicateCheckResponse comprehensiveDuplicateCheck(GuestCreateRequest request) {
        return duplicateDetectionService.comprehensiveDuplicateCheck(request);
    }
    
    // Private helper methods
    
    private Staff getCurrentStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "email", email));
    }
    
    private void mapRequestToEntity(GuestCreateRequest request, Guest guest) {
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setEmail(request.getEmail());
        guest.setSeatingPreference(request.getSeatingPreference());
        guest.setDietaryRestrictions(request.getDietaryRestrictions());
        guest.setFavoriteDrinks(request.getFavoriteDrinks());
        guest.setBirthday(request.getBirthday());
        guest.setAnniversary(request.getAnniversary());
        guest.setNotes(request.getNotes());
    }
    
    private void mapRequestToEntity(GuestUpdateRequest request, Guest guest) {
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setEmail(request.getEmail());
        guest.setSeatingPreference(request.getSeatingPreference());
        guest.setDietaryRestrictions(request.getDietaryRestrictions());
        guest.setFavoriteDrinks(request.getFavoriteDrinks());
        guest.setBirthday(request.getBirthday());
        guest.setAnniversary(request.getAnniversary());
        guest.setNotes(request.getNotes());
    }
    
    private GuestResponse mapEntityToResponse(Guest guest) {
        GuestResponse response = new GuestResponse();
        response.setId(guest.getId());
        response.setFirstName(guest.getFirstName());
        response.setLastName(guest.getLastName());
        response.setPhone(guest.getPhone());
        response.setEmail(guest.getEmail());
        response.setPhotoUrl(guest.getPhotoUrl());
        response.setSeatingPreference(guest.getSeatingPreference());
        response.setDietaryRestrictions(guest.getDietaryRestrictions());
        response.setFavoriteDrinks(guest.getFavoriteDrinks());
        response.setBirthday(guest.getBirthday());
        response.setAnniversary(guest.getAnniversary());
        response.setNotes(guest.getNotes());
        response.setCreatedAt(guest.getCreatedAt());
        response.setUpdatedAt(guest.getUpdatedAt());
        
        // Set visit information
        Visit lastVisit = guest.getLastVisit();
        if (lastVisit != null) {
            response.setLastVisit(lastVisit.getCreatedAt());
        }
        response.setVisitCount(guest.getVisitCount());
        
        // Set created by information
        if (guest.getCreatedBy() != null) {
            response.setCreatedByName(guest.getCreatedBy().getFirstName() + " " + guest.getCreatedBy().getLastName());
        }
        
        return response;
    }
    
    private boolean hasAdvancedFilters(GuestSearchRequest request) {
        return request.getSeatingPreference() != null ||
               request.getHasBirthday() != null ||
               request.getHasAnniversary() != null;
    }
    
    private boolean hasComplexFilters(GuestSearchRequest request) {
        return hasAdvancedFilters(request) ||
               (request.getDietaryRestrictions() != null && !request.getDietaryRestrictions().isEmpty()) ||
               (request.getFavoriteDrinks() != null && !request.getFavoriteDrinks().isEmpty());
    }
    
    private Page<Guest> convertListToPage(List<Guest> guests, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), guests.size());
        
        List<Guest> pageContent = guests.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, guests.size());
    }
    
    private void deleteOldPhoto(String photoUrl) {
        try {
            // Extract filename from URL
            // Assuming URL format: http://localhost:8080/api/files/guests/filename
            if (photoUrl.contains("/guests/")) {
                String filename = photoUrl.substring(photoUrl.lastIndexOf("/guests/") + 8);
                String relativePath = "guests/" + filename;
                fileUploadService.deleteFile(relativePath);
            }
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to delete old photo: " + e.getMessage());
        }
    }
}