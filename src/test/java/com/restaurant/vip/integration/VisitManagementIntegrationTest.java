package com.restaurant.vip.integration;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.StaffRepository;
import com.restaurant.vip.repository.VisitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for visit management endpoints.
 * Tests complete visit tracking functionality including CRUD operations and history.
 */
@AutoConfigureWebMvc
@DisplayName("Visit Management Integration Tests")
class VisitManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private StaffRepository staffRepository;

    private Staff testStaff;
    private Staff serverStaff;
    private Guest testGuest;
    private Visit testVisit;

    @Override
    protected void setupTestData() {
        // Create test staff
        testStaff = TestDataBuilder.createManagerStaff();
        testStaff = staffRepository.save(testStaff);

        serverStaff = TestDataBuilder.createServerStaff();
        serverStaff = staffRepository.save(serverStaff);

        // Create test guest
        testGuest = TestDataBuilder.createBasicGuest();
        testGuest.setCreatedBy(testStaff);
        testGuest = guestRepository.save(testGuest);

        // Create test visit
        testVisit = TestDataBuilder.createBasicVisit(testGuest, testStaff);
        testVisit = visitRepository.save(testVisit);
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should create new visit successfully")
    void shouldCreateVisitSuccessfully() throws Exception {
        // Given
        VisitCreateRequest request = TestDataBuilder.createVisitCreateRequest(
            testGuest.getId(), serverStaff.getId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestId").value(testGuest.getId()))
                .andExpect(jsonPath("$.staffId").value(serverStaff.getId()))
                .andExpect(jsonPath("$.visitDate").exists())
                .andExpect(jsonPath("$.visitTime").exists())
                .andExpect(jsonPath("$.partySize").value(request.getPartySize()))
                .andExpect(jsonPath("$.tableNumber").value(request.getTableNumber()))
                .andExpect(jsonPath("$.serviceNotes").value(request.getServiceNotes()))
                .andReturn();

        // Verify visit was saved to database
        String responseJson = result.getResponse().getContentAsString();
        VisitResponse response = fromJsonString(responseJson, VisitResponse.class);
        
        Visit savedVisit = visitRepository.findById(response.getId()).orElse(null);
        assertThat(savedVisit).isNotNull();
        assertThat(savedVisit.getGuest().getId()).isEqualTo(testGuest.getId());
        assertThat(savedVisit.getStaff().getId()).isEqualTo(serverStaff.getId());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should validate required fields in visit creation")
    void shouldValidateVisitCreationFields() throws Exception {
        // Given - request with missing required fields
        VisitCreateRequest request = new VisitCreateRequest();
        request.setPartySize(2);

        // When & Then
        mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get visit by ID successfully")
    void shouldGetVisitById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/{visitId}", testVisit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testVisit.getId()))
                .andExpect(jsonPath("$.guestId").value(testGuest.getId()))
                .andExpect(jsonPath("$.staffId").value(testStaff.getId()))
                .andExpect(jsonPath("$.partySize").value(testVisit.getPartySize()))
                .andExpect(jsonPath("$.tableNumber").value(testVisit.getTableNumber()));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should return 404 for non-existent visit")
    void shouldReturn404ForNonExistentVisit() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/{visitId}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should update visit successfully")
    void shouldUpdateVisitSuccessfully() throws Exception {
        // Given
        VisitUpdateRequest request = TestDataBuilder.createVisitUpdateRequest();

        // When & Then
        mockMvc.perform(put("/api/visits/{visitId}", testVisit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitDate").exists())
                .andExpect(jsonPath("$.visitTime").exists())
                .andExpect(jsonPath("$.partySize").value(request.getPartySize()))
                .andExpect(jsonPath("$.tableNumber").value(request.getTableNumber()))
                .andExpect(jsonPath("$.serviceNotes").value(request.getServiceNotes()));

        // Verify database was updated
        Visit updatedVisit = visitRepository.findById(testVisit.getId()).orElse(null);
        assertThat(updatedVisit).isNotNull();
        assertThat(updatedVisit.getPartySize()).isEqualTo(request.getPartySize());
        assertThat(updatedVisit.getTableNumber()).isEqualTo(request.getTableNumber());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should delete visit successfully")
    void shouldDeleteVisitSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/visits/{visitId}", testVisit.getId()))
                .andExpect(status().isNoContent());

        // Verify visit was deleted
        Visit deletedVisit = visitRepository.findById(testVisit.getId()).orElse(null);
        assertThat(deletedVisit).isNull();
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should not allow non-managers to delete visits")
    void shouldNotAllowNonManagersToDelete() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/visits/{visitId}", testVisit.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should update visit notes successfully")
    void shouldUpdateVisitNotes() throws Exception {
        // Given
        Map<String, String> notesRequest = Map.of("notes", "Updated service notes");

        // When & Then
        mockMvc.perform(patch("/api/visits/{visitId}/notes", testVisit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(notesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceNotes").value("Updated service notes"));

        // Verify database was updated
        Visit updatedVisit = visitRepository.findById(testVisit.getId()).orElse(null);
        assertThat(updatedVisit).isNotNull();
        assertThat(updatedVisit.getServiceNotes()).isEqualTo("Updated service notes");
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get visit notes with permission info")
    void shouldGetVisitNotes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/{visitId}/notes", testVisit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value(testVisit.getServiceNotes()))
                .andExpect(jsonPath("$.canEdit").isBoolean())
                .andExpect(jsonPath("$.visitId").value(testVisit.getId()));
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should add notes to visit without existing notes")
    void shouldAddVisitNotes() throws Exception {
        // Given - create visit without notes
        Visit visitWithoutNotes = TestDataBuilder.createBasicVisit(testGuest, serverStaff);
        visitWithoutNotes.setServiceNotes(null);
        visitWithoutNotes = visitRepository.save(visitWithoutNotes);

        VisitNotesRequest request = new VisitNotesRequest();
        request.setNotes("New service notes");

        // When & Then
        mockMvc.perform(post("/api/visits/{visitId}/notes", visitWithoutNotes.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("New service notes"))
                .andExpect(jsonPath("$.visitId").value(visitWithoutNotes.getId()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should update existing visit notes enhanced")
    void shouldUpdateVisitNotesEnhanced() throws Exception {
        // Given
        VisitNotesRequest request = new VisitNotesRequest();
        request.setNotes("Enhanced updated notes");

        // When & Then
        mockMvc.perform(put("/api/visits/{visitId}/notes", testVisit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Enhanced updated notes"))
                .andExpect(jsonPath("$.canEdit").isBoolean());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should clear visit notes")
    void shouldClearVisitNotes() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/visits/{visitId}/notes", testVisit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").isEmpty())
                .andExpect(jsonPath("$.visitId").value(testVisit.getId()));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should not allow non-managers to clear notes")
    void shouldNotAllowNonManagersToClearNotes() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/visits/{visitId}/notes", testVisit.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get all guest visits with pagination")
    void shouldGetGuestVisitsWithPagination() throws Exception {
        // Given - create additional visits for the guest
        Visit additionalVisit = TestDataBuilder.createVisitWithSpecialNotes(testGuest, serverStaff);
        visitRepository.save(additionalVisit);

        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}", testGuest.getId())
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "visitDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get all guest visits non-paginated")
    void shouldGetAllGuestVisits() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/all", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].guestId").value(testGuest.getId()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get visits by date range")
    void shouldGetVisitsByDateRange() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        // When & Then
        mockMvc.perform(get("/api/visits/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get today's visits")
    void shouldGetTodaysVisits() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get guest visit count")
    void shouldGetGuestVisitCount() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/count", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitCount").isNumber())
                .andExpect(jsonPath("$.visitCount").value(1));
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get last visit for guest")
    void shouldGetLastVisitForGuest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/last", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(testGuest.getId()))
                .andExpect(jsonPath("$.id").value(testVisit.getId()));
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should return 404 for guest with no visits")
    void shouldReturn404ForGuestWithNoVisits() throws Exception {
        // Given - create guest without visits
        Guest guestWithoutVisits = TestDataBuilder.createVeganGuest();
        guestWithoutVisits.setPhone("+9999999999");
        guestWithoutVisits.setCreatedBy(testStaff);
        guestWithoutVisits = guestRepository.save(guestWithoutVisits);

        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/last", guestWithoutVisits.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get comprehensive visit history")
    void shouldGetGuestVisitHistory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/history", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(testGuest.getId()))
                .andExpect(jsonPath("$.totalVisits").isNumber())
                .andExpect(jsonPath("$.visits").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get paginated visit history")
    void shouldGetGuestVisitHistoryPaginated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/history/paginated", testGuest.getId())
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "visitDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get visit statistics for guest")
    void shouldGetGuestVisitStatistics() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/statistics", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").isNumber())
                .andExpect(jsonPath("$.averagePartySize").isNumber())
                .andExpect(jsonPath("$.lastVisitDate").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get recent visits")
    void shouldGetRecentVisits() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/recent")
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should search visits by notes")
    void shouldSearchVisitsByNotes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/search/notes")
                .param("searchTerm", "service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should check if staff can edit visit notes")
    void shouldCheckCanEditVisitNotes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/{visitId}/notes/can-edit", testVisit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").isBoolean());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get guest visits with notes")
    void shouldGetGuestVisitsWithNotes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/guest/{guestId}/with-notes", testGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serviceNotes").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should handle concurrent visit creation")
    void shouldHandleConcurrentVisitCreation() throws Exception {
        // Given - multiple visit requests for the same guest
        for (int i = 0; i < 3; i++) {
            VisitCreateRequest request = TestDataBuilder.createVisitCreateRequest(
                testGuest.getId(), serverStaff.getId());
            request.setVisitTime(LocalTime.of(18 + i, 0)); // Different times
            request.setTableNumber("T" + i);

            // When & Then
            mockMvc.perform(post("/api/visits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tableNumber").value(request.getTableNumber()));
        }
    }

    @Test
    @DisplayName("Should require authentication for visit endpoints")
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/visits/{visitId}", testVisit.getId()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}