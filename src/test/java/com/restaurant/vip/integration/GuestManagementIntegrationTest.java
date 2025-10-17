package com.restaurant.vip.integration;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for guest management endpoints.
 * Tests complete CRUD operations and search functionality for guests.
 */
@AutoConfigureWebMvc
@DisplayName("Guest Management Integration Tests")
class GuestManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private StaffRepository staffRepository;

    private Staff testStaff;
    private Guest testGuest;
    private Guest birthdayGuest;
    private Guest veganGuest;

    @Override
    protected void setupTestData() {
        // Create test staff
        testStaff = TestDataBuilder.createManagerStaff();
        testStaff = staffRepository.save(testStaff);

        // Create test guests
        testGuest = TestDataBuilder.createBasicGuest();
        testGuest.setCreatedBy(testStaff);
        testGuest = guestRepository.save(testGuest);

        birthdayGuest = TestDataBuilder.createGuestWithUpcomingBirthday();
        birthdayGuest.setCreatedBy(testStaff);
        birthdayGuest = guestRepository.save(birthdayGuest);

        veganGuest = TestDataBuilder.createVeganGuest();
        veganGuest.setCreatedBy(testStaff);
        veganGuest = guestRepository.save(veganGuest);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should create new guest successfully")
    void shouldCreateGuestSuccessfully() throws Exception {
        // Given
        GuestCreateRequest request = TestDataBuilder.createGuestCreateRequest();

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.phone").value(request.getPhone()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.seatingPreference").value(request.getSeatingPreference()))
                .andExpect(jsonPath("$.dietaryRestrictions").isArray())
                .andExpect(jsonPath("$.favoriteDrinks").isArray())
                .andReturn();

        // Verify guest was saved to database
        String responseJson = result.getResponse().getContentAsString();
        GuestResponse response = fromJsonString(responseJson, GuestResponse.class);
        
        Guest savedGuest = guestRepository.findById(response.getId()).orElse(null);
        assertThat(savedGuest).isNotNull();
        assertThat(savedGuest.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(savedGuest.getPhone()).isEqualTo(request.getPhone());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should reject guest creation with duplicate phone")
    void shouldRejectDuplicatePhone() throws Exception {
        // Given - request with existing phone number
        GuestCreateRequest request = TestDataBuilder.createGuestCreateRequest();
        request.setPhone(testGuest.getPhone());

        // When & Then
        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should validate required fields in guest creation")
    void shouldValidateRequiredFields() throws Exception {
        // Given - request with missing required fields
        GuestCreateRequest request = new GuestCreateRequest();
        request.setEmail("test@email.com");

        // When & Then
        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get guest by ID successfully")
    void shouldGetGuestById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/{id}", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testGuest.getId()))
                .andExpect(jsonPath("$.firstName").value(testGuest.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testGuest.getLastName()))
                .andExpect(jsonPath("$.phone").value(testGuest.getPhone()))
                .andExpect(jsonPath("$.email").value(testGuest.getEmail()));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should return 404 for non-existent guest")
    void shouldReturn404ForNonExistentGuest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should update guest successfully")
    void shouldUpdateGuestSuccessfully() throws Exception {
        // Given
        GuestUpdateRequest request = TestDataBuilder.createGuestUpdateRequest();

        // When & Then
        mockMvc.perform(put("/api/guests/{id}", testGuest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.phone").value(request.getPhone()))
                .andExpect(jsonPath("$.seatingPreference").value(request.getSeatingPreference()));

        // Verify database was updated
        Guest updatedGuest = guestRepository.findById(testGuest.getId()).orElse(null);
        assertThat(updatedGuest).isNotNull();
        assertThat(updatedGuest.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(updatedGuest.getSeatingPreference()).isEqualTo(request.getSeatingPreference());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should delete guest successfully")
    void shouldDeleteGuestSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/guests/{id}", testGuest.getId()))
                .andExpect(status().isNoContent());

        // Verify guest was soft deleted
        Guest deletedGuest = guestRepository.findById(testGuest.getId()).orElse(null);
        assertThat(deletedGuest).isNull(); // Assuming soft delete removes from findById
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should not allow non-managers to delete guests")
    void shouldNotAllowNonManagersToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/guests/{id}", testGuest.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get all guests with pagination")
    void shouldGetAllGuestsWithPagination() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "firstName")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should search guests by name")
    void shouldSearchGuestsByName() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/search")
                .param("q", "John")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should search guests by phone number")
    void shouldSearchGuestsByPhone() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/search")
                .param("q", testGuest.getPhone())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].phone").value(testGuest.getPhone()));
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should perform advanced search with filters")
    void shouldPerformAdvancedSearch() throws Exception {
        // Given
        GuestSearchRequest searchRequest = TestDataBuilder.createDietaryFilterRequest(
            Arrays.asList("Vegetarian"));

        // When & Then
        mockMvc.perform(post("/api/guests/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].dietaryRestrictions").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should check phone number existence")
    void shouldCheckPhoneExistence() throws Exception {
        // When & Then - existing phone
        mockMvc.perform(get("/api/guests/check-phone")
                .param("phone", testGuest.getPhone()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.phone").value(testGuest.getPhone()))
                .andExpect(jsonPath("$.existingGuest").exists());

        // When & Then - non-existing phone
        mockMvc.perform(get("/api/guests/check-phone")
                .param("phone", "+9999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.phone").value("+9999999999"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get guests with upcoming occasions")
    void shouldGetGuestsWithUpcomingOccasions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/upcoming-occasions")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should filter guests by dietary restrictions")
    void shouldFilterByDietaryRestrictions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/filter/dietary")
                .param("restrictions", "Vegan", "Gluten-free")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should filter guests by favorite drinks")
    void shouldFilterByFavoriteDrinks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/filter/drinks")
                .param("drinks", "Red wine", "Beer")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should filter guests by seating preference")
    void shouldFilterBySeatingPreference() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/filter/seating")
                .param("preference", "Window table")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should filter guests by occasions")
    void shouldFilterByOccasions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/filter/occasions")
                .param("hasBirthday", "true")
                .param("hasAnniversary", "false")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should check for potential duplicates")
    void shouldCheckPotentialDuplicates() throws Exception {
        // Given - request similar to existing guest
        GuestCreateRequest request = TestDataBuilder.createGuestCreateRequest();
        request.setFirstName("John"); // Similar to existing guest
        request.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/guests/check-duplicates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.potentialDuplicates").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should perform comprehensive duplicate check")
    void shouldPerformComprehensiveDuplicateCheck() throws Exception {
        // Given
        GuestCreateRequest request = TestDataBuilder.createGuestCreateRequest();
        request.setPhone(testGuest.getPhone()); // Exact duplicate phone

        // When & Then
        mockMvc.perform(post("/api/guests/comprehensive-duplicate-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.potentialDuplicates").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should handle concurrent guest creation")
    void shouldHandleConcurrentGuestCreation() throws Exception {
        // Given - multiple unique guest requests
        for (int i = 0; i < 3; i++) {
            GuestCreateRequest request = TestDataBuilder.createGuestCreateRequest();
            request.setPhone("+123456789" + i);
            request.setEmail("concurrent" + i + "@email.com");

            // When & Then
            mockMvc.perform(post("/api/guests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.phone").value(request.getPhone()));
        }
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should handle empty search results gracefully")
    void shouldHandleEmptySearchResults() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests/search")
                .param("q", "NonExistentGuest")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should require authentication for guest endpoints")
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}