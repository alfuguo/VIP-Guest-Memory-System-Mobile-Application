package com.restaurant.vip.service;

import com.restaurant.vip.dto.DuplicateCheckResponse;
import com.restaurant.vip.dto.GuestCreateRequest;
import com.restaurant.vip.dto.GuestResponse;
import com.restaurant.vip.dto.GuestUpdateRequest;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DuplicateDetectionService {
    
    private final GuestRepository guestRepository;
    
    @Autowired
    public DuplicateDetectionService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }
    
    /**
     * Check for exact duplicate by phone number
     */
    public DuplicateCheckResponse checkExactDuplicate(String phone) {
        DuplicateCheckResponse response = new DuplicateCheckResponse();
        response.setPhone(phone);
        
        Optional<Guest> existingGuest = guestRepository.findByPhone(phone);
        if (existingGuest.isPresent()) {
            Guest guest = existingGuest.get();
            response.setExists(true);
            response.setMessage("A guest with this phone number already exists: " + guest.getFullName());
            response.setExistingGuest(mapGuestToResponse(guest));
        } else {
            response.setExists(false);
            response.setMessage("No guest found with this phone number");
        }
        
        return response;
    }
    
    /**
     * Check for exact duplicate by phone number for update (excluding current guest)
     */
    public DuplicateCheckResponse checkExactDuplicateForUpdate(String phone, Long currentGuestId) {
        DuplicateCheckResponse response = new DuplicateCheckResponse();
        response.setPhone(phone);
        
        boolean exists = guestRepository.existsByPhoneAndIdNot(phone, currentGuestId);
        if (exists) {
            Optional<Guest> existingGuest = guestRepository.findByPhone(phone);
            if (existingGuest.isPresent()) {
                Guest guest = existingGuest.get();
                response.setExists(true);
                response.setMessage("Another guest with this phone number already exists: " + guest.getFullName());
                response.setExistingGuest(mapGuestToResponse(guest));
            }
        } else {
            response.setExists(false);
            response.setMessage("No other guest found with this phone number");
        }
        
        return response;
    }
    
    /**
     * Check for potential duplicates based on name similarity
     */
    public List<GuestResponse> findPotentialDuplicates(GuestCreateRequest request) {
        List<Guest> potentialDuplicates = new ArrayList<>();
        
        // Search by similar names
        String searchTerm = request.getFirstName();
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            searchTerm += " " + request.getLastName();
        }
        
        // Find guests with similar names (case-insensitive partial match)
        List<Guest> nameMatches = guestRepository.searchByName(searchTerm, 
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        
        // Filter out exact phone matches (those will be caught by exact duplicate check)
        for (Guest guest : nameMatches) {
            if (!guest.getPhone().equals(request.getPhone())) {
                // Check name similarity
                if (isNameSimilar(request.getFirstName(), request.getLastName(), 
                                guest.getFirstName(), guest.getLastName())) {
                    potentialDuplicates.add(guest);
                }
            }
        }
        
        // Also check for similar phone numbers (different formatting)
        if (request.getPhone() != null) {
            String normalizedPhone = normalizePhoneNumber(request.getPhone());
            List<Guest> phoneMatches = guestRepository.searchByPhone(normalizedPhone, 
                    org.springframework.data.domain.PageRequest.of(0, 5)).getContent();
            
            for (Guest guest : phoneMatches) {
                if (!guest.getPhone().equals(request.getPhone()) && 
                    !potentialDuplicates.contains(guest)) {
                    String existingNormalized = normalizePhoneNumber(guest.getPhone());
                    if (existingNormalized.equals(normalizedPhone)) {
                        potentialDuplicates.add(guest);
                    }
                }
            }
        }
        
        return potentialDuplicates.stream()
                .map(this::mapGuestToResponse)
                .toList();
    }
    
    /**
     * Check for potential duplicates for update
     */
    public List<GuestResponse> findPotentialDuplicatesForUpdate(GuestUpdateRequest request, Long currentGuestId) {
        List<Guest> potentialDuplicates = new ArrayList<>();
        
        // Search by similar names
        String searchTerm = request.getFirstName();
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            searchTerm += " " + request.getLastName();
        }
        
        // Find guests with similar names (case-insensitive partial match)
        List<Guest> nameMatches = guestRepository.searchByName(searchTerm, 
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        
        // Filter out current guest and exact phone matches
        for (Guest guest : nameMatches) {
            if (!guest.getId().equals(currentGuestId) && !guest.getPhone().equals(request.getPhone())) {
                // Check name similarity
                if (isNameSimilar(request.getFirstName(), request.getLastName(), 
                                guest.getFirstName(), guest.getLastName())) {
                    potentialDuplicates.add(guest);
                }
            }
        }
        
        return potentialDuplicates.stream()
                .map(this::mapGuestToResponse)
                .toList();
    }
    
    /**
     * Comprehensive duplicate check for creation
     */
    public DuplicateCheckResponse comprehensiveDuplicateCheck(GuestCreateRequest request) {
        DuplicateCheckResponse response = new DuplicateCheckResponse();
        response.setPhone(request.getPhone());
        
        // First check for exact duplicate
        DuplicateCheckResponse exactCheck = checkExactDuplicate(request.getPhone());
        if (exactCheck.isExists()) {
            return exactCheck;
        }
        
        // Check for potential duplicates
        List<GuestResponse> potentialDuplicates = findPotentialDuplicates(request);
        
        response.setExists(false);
        if (!potentialDuplicates.isEmpty()) {
            response.setMessage(String.format("Found %d potential duplicate(s) with similar information", 
                                            potentialDuplicates.size()));
            // For now, we'll just return the first potential duplicate
            // In a real implementation, you might want to return all of them
            response.setExistingGuest(potentialDuplicates.get(0));
        } else {
            response.setMessage("No duplicates found");
        }
        
        return response;
    }
    
    // Private helper methods
    
    private boolean isNameSimilar(String firstName1, String lastName1, String firstName2, String lastName2) {
        // Simple similarity check - can be enhanced with more sophisticated algorithms
        String name1 = (firstName1 + " " + (lastName1 != null ? lastName1 : "")).trim().toLowerCase();
        String name2 = (firstName2 + " " + (lastName2 != null ? lastName2 : "")).trim().toLowerCase();
        
        // Check if names are exactly the same
        if (name1.equals(name2)) {
            return true;
        }
        
        // Check if first names match exactly
        if (firstName1.toLowerCase().equals(firstName2.toLowerCase())) {
            return true;
        }
        
        // Check if full names have significant overlap
        String[] words1 = name1.split("\\s+");
        String[] words2 = name2.split("\\s+");
        
        int matches = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2) && word1.length() > 2) { // Only count meaningful words
                    matches++;
                    break;
                }
            }
        }
        
        // Consider similar if at least 2 words match or if it's a single name match with length > 3
        return matches >= 2 || (matches >= 1 && firstName1.length() > 3);
    }
    
    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        // Remove all non-digit characters except +
        return phone.replaceAll("[^+\\d]", "");
    }
    
    private GuestResponse mapGuestToResponse(Guest guest) {
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
        response.setVisitCount(guest.getVisitCount());
        
        if (guest.getCreatedBy() != null) {
            response.setCreatedByName(guest.getCreatedBy().getFirstName() + " " + guest.getCreatedBy().getLastName());
        }
        
        return response;
    }
}