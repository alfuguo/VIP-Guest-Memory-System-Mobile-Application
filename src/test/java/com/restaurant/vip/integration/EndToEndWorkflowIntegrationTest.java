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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests that simulate complete user workflows.
 * Tests realistic scenarios from authentication through guest management and visit tracking.
 */
@AutoConfigureWebMvc
@DisplayName("End-to-End Workflow Integration Tests")
class EndToEndWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private VisitRepository visitRepository;

    private Staff managerStaff;
    private Staff hostStaff;
    private Staff serverStaff;

    @Override
    protected void setupTestData() {
        // Create different staff roles
        managerStaff = TestDataBuilder.createManagerStaff();
        managerStaff = staffRepository.save(managerStaff);

        hostStaff = TestDataBuilder.createHostStaff();
        hostStaff = staffRepository.save(hostStaff);

        serverStaff = TestDataBuilder.createServerStaff();
        serverStaff = staffRepository.save(serverStaff);
    }

    @Test
    @DisplayName("Complete new guest workflow: Authentication -> Create Guest -> Log Visit -> View History")
    void completeNewGuestWorkflow() throws Exception {
        // Step 1: Host logs in
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            hostStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String authToken = "Bearer " + loginResponse.getAccessToken();

        // Step 2: Check if phone number already exists (duplicate prevention)
        String newGuestPhone = "+1555123456";
        mockMvc.perform(get("/api/guests/check-phone")
                .param("phone", newGuestPhone)
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));

        // Step 3: Create new guest profile
        GuestCreateRequest guestRequest = TestDataBuilder.createGuestCreateRequest();
        guestRequest.setPhone(newGuestPhone);
        guestRequest.setFirstName("John");
        guestRequest.setLastName("NewCustomer");
        guestRequest.setEmail("john.newcustomer@email.com");

        MvcResult guestResult = mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(guestRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("NewCustomer"))
                .andReturn();

        GuestResponse createdGuest = fromJsonString(
            guestResult.getResponse().getContentAsString(), GuestResponse.class);

        // Step 4: Log first visit for the new guest
        VisitCreateRequest visitRequest = TestDataBuilder.createVisitCreateRequest(
            createdGuest.getId(), hostStaff.getId());
        visitRequest.setVisitDate(LocalDate.now());
        visitRequest.setVisitTime(LocalTime.of(19, 30));
        visitRequest.setPartySize(2);
        visitRequest.setTableNumber("A5");
        visitRequest.setServiceNotes("First visit - excellent experience");

        MvcResult visitResult = mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(visitRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestId").value(createdGuest.getId()))
                .andExpect(jsonPath("$.serviceNotes").value("First visit - excellent experience"))
                .andReturn();

        VisitResponse createdVisit = fromJsonString(
            visitResult.getResponse().getContentAsString(), VisitResponse.class);

        // Step 5: View guest profile with visit history
        mockMvc.perform(get("/api/guests/{id}", createdGuest.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.visitCount").value(1))
                .andExpect(jsonPath("$.lastVisit").exists());

        // Step 6: Get visit history for the guest
        mockMvc.perform(get("/api/visits/guest/{guestId}/history", createdGuest.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(createdGuest.getId()))
                .andExpect(jsonPath("$.totalVisits").value(1))
                .andExpect(jsonPath("$.visits").isArray())
                .andExpect(jsonPath("$.visits[0].serviceNotes").value("First visit - excellent experience"));

        // Verify data persistence
        Guest savedGuest = guestRepository.findById(createdGuest.getId()).orElse(null);
        assertThat(savedGuest).isNotNull();
        assertThat(savedGuest.getFirstName()).isEqualTo("John");

        Visit savedVisit = visitRepository.findById(createdVisit.getId()).orElse(null);
        assertThat(savedVisit).isNotNull();
        assertThat(savedVisit.getGuest().getId()).isEqualTo(createdGuest.getId());
    }

    @Test
    @DisplayName("Returning guest workflow: Search -> Update Preferences -> Log Return Visit -> Check Notifications")
    void returningGuestWorkflow() throws Exception {
        // Setup: Create existing guest with old visit
        Guest existingGuest = TestDataBuilder.createBasicGuest();
        existingGuest.setCreatedBy(managerStaff);
        existingGuest = guestRepository.save(existingGuest);

        Visit oldVisit = TestDataBuilder.createBasicVisit(existingGuest, serverStaff);
        oldVisit.setVisitDate(LocalDate.now().minusMonths(8)); // 8 months ago
        visitRepository.save(oldVisit);

        // Step 1: Server logs in
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            serverStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String authToken = "Bearer " + loginResponse.getAccessToken();

        // Step 2: Search for returning guest by phone
        mockMvc.perform(get("/api/guests/search")
                .param("q", existingGuest.getPhone())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].phone").value(existingGuest.getPhone()));

        // Step 3: Check notifications for returning guests
        mockMvc.perform(get("/api/notifications/upcoming")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returningGuests").isArray())
                .andExpect(jsonPath("$.returningGuests[?(@.id == " + existingGuest.getId() + ")]").exists());

        // Step 4: Update guest preferences (they mentioned new dietary restrictions)
        GuestUpdateRequest updateRequest = new GuestUpdateRequest();
        updateRequest.setFirstName(existingGuest.getFirstName());
        updateRequest.setLastName(existingGuest.getLastName());
        updateRequest.setPhone(existingGuest.getPhone());
        updateRequest.setEmail(existingGuest.getEmail());
        updateRequest.setSeatingPreference("Quiet corner booth");
        updateRequest.setDietaryRestrictions(java.util.Arrays.asList("Gluten-free", "No dairy"));
        updateRequest.setFavoriteDrinks(java.util.Arrays.asList("Sparkling water", "Herbal tea"));
        updateRequest.setNotes("Updated preferences - now gluten-free");

        mockMvc.perform(put("/api/guests/{id}", existingGuest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatingPreference").value("Quiet corner booth"))
                .andExpect(jsonPath("$.dietaryRestrictions").isArray())
                .andExpect(jsonPath("$.dietaryRestrictions[0]").value("Gluten-free"));

        // Step 5: Log return visit
        VisitCreateRequest returnVisitRequest = TestDataBuilder.createVisitCreateRequest(
            existingGuest.getId(), serverStaff.getId());
        returnVisitRequest.setServiceNotes("Welcome back! Accommodated new gluten-free requirements");
        returnVisitRequest.setTableNumber("B7"); // Quiet corner booth

        mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(returnVisitRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceNotes").value("Welcome back! Accommodated new gluten-free requirements"));

        // Step 6: Verify guest is no longer flagged as returning
        mockMvc.perform(get("/api/notifications/upcoming")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returningGuests[?(@.id == " + existingGuest.getId() + ")]").doesNotExist());

        // Step 7: Check updated visit statistics
        mockMvc.perform(get("/api/visits/guest/{guestId}/statistics", existingGuest.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(2))
                .andExpect(jsonPath("$.daysSinceLastVisit").value(0));
    }

    @Test
    @DisplayName("Special occasion workflow: Create Guest with Birthday -> Check Notifications -> Acknowledge")
    void specialOccasionWorkflow() throws Exception {
        // Step 1: Manager logs in
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            managerStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String authToken = "Bearer " + loginResponse.getAccessToken();

        // Step 2: Create guest with upcoming birthday
        GuestCreateRequest birthdayGuestRequest = TestDataBuilder.createGuestCreateRequest();
        birthdayGuestRequest.setFirstName("Birthday");
        birthdayGuestRequest.setLastName("Celebrant");
        birthdayGuestRequest.setPhone("+1555987654");
        birthdayGuestRequest.setBirthday(LocalDate.now().plusDays(3)); // Birthday in 3 days
        birthdayGuestRequest.setFavoriteDrinks(java.util.Arrays.asList("Champagne", "Red wine"));

        MvcResult guestResult = mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(birthdayGuestRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andReturn();

        GuestResponse birthdayGuest = fromJsonString(
            guestResult.getResponse().getContentAsString(), GuestResponse.class);

        // Step 3: Check special occasion notifications
        mockMvc.perform(get("/api/notifications/special-occasions")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingBirthdays").isArray())
                .andExpect(jsonPath("$.upcomingBirthdays[?(@.firstName == 'Birthday')]").exists())
                .andExpect(jsonPath("$.upcomingBirthdays[?(@.firstName == 'Birthday')].favoriteDrinks").exists());

        // Step 4: Get upcoming notifications (includes special occasions)
        mockMvc.perform(get("/api/notifications/upcoming")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialOccasions").isArray())
                .andExpect(jsonPath("$.specialOccasions[?(@.firstName == 'Birthday')]").exists());

        // Step 5: Acknowledge the birthday notification
        mockMvc.perform(post("/api/notifications/{id}/acknowledge", birthdayGuest.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedAt").exists());

        // Step 6: Log visit on birthday with special notes
        VisitCreateRequest birthdayVisitRequest = TestDataBuilder.createVisitCreateRequest(
            birthdayGuest.getId(), managerStaff.getId());
        birthdayVisitRequest.setVisitDate(LocalDate.now().plusDays(3)); // Birthday date
        birthdayVisitRequest.setServiceNotes("Birthday celebration! Provided complimentary champagne and dessert");

        mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(birthdayVisitRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceNotes").value("Birthday celebration! Provided complimentary champagne and dessert"));
    }

    @Test
    @DisplayName("Search and filter workflow: Multiple Guests -> Advanced Search -> Filter by Preferences")
    void searchAndFilterWorkflow() throws Exception {
        // Setup: Create multiple guests with different preferences
        Guest veganGuest = TestDataBuilder.createVeganGuest();
        veganGuest.setCreatedBy(hostStaff);
        veganGuest = guestRepository.save(veganGuest);

        Guest wineGuest = TestDataBuilder.createBasicGuest();
        wineGuest.setFirstName("Wine");
        wineGuest.setLastName("Lover");
        wineGuest.setPhone("+1555111222");
        wineGuest.setFavoriteDrinks(java.util.Arrays.asList("Red wine", "Pinot Noir"));
        wineGuest.setSeatingPreference("Wine bar");
        wineGuest.setCreatedBy(hostStaff);
        wineGuest = guestRepository.save(wineGuest);

        // Step 1: Host logs in
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            hostStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String authToken = "Bearer " + loginResponse.getAccessToken();

        // Step 2: Search by dietary restrictions
        mockMvc.perform(get("/api/guests/filter/dietary")
                .param("restrictions", "Vegan")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.firstName == 'Vegan')]").exists());

        // Step 3: Search by favorite drinks
        mockMvc.perform(get("/api/guests/filter/drinks")
                .param("drinks", "Red wine")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.firstName == 'Wine')]").exists());

        // Step 4: Advanced search with multiple criteria
        GuestSearchRequest advancedSearch = new GuestSearchRequest();
        advancedSearch.setFavoriteDrinks(java.util.Arrays.asList("Red wine"));
        advancedSearch.setSeatingPreference("Wine bar");
        advancedSearch.setPage(0);
        advancedSearch.setSize(20);

        mockMvc.perform(post("/api/guests/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(advancedSearch))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("Wine"));

        // Step 5: Quick search by name
        mockMvc.perform(get("/api/guests/search")
                .param("q", "Vegan")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("Vegan"));
    }

    @Test
    @DisplayName("Role-based access workflow: Different roles accessing same resources")
    void roleBasedAccessWorkflow() throws Exception {
        // Setup: Create a guest
        Guest testGuest = TestDataBuilder.createBasicGuest();
        testGuest.setCreatedBy(managerStaff);
        testGuest = guestRepository.save(testGuest);

        // Test Manager access
        LoginRequest managerLogin = TestDataBuilder.createLoginRequest(
            managerStaff.getEmail(), "password123");
        MvcResult managerLoginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(managerLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String managerToken = "Bearer " + fromJsonString(
            managerLoginResult.getResponse().getContentAsString(), LoginResponse.class).getAccessToken();

        // Manager can delete guests
        mockMvc.perform(delete("/api/guests/{id}", testGuest.getId())
                .header("Authorization", managerToken))
                .andExpect(status().isNoContent());

        // Recreate guest for host test
        testGuest = TestDataBuilder.createBasicGuest();
        testGuest.setPhone("+1555999888");
        testGuest.setCreatedBy(managerStaff);
        testGuest = guestRepository.save(testGuest);

        // Test Host access
        LoginRequest hostLogin = TestDataBuilder.createLoginRequest(
            hostStaff.getEmail(), "password123");
        MvcResult hostLoginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(hostLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String hostToken = "Bearer " + fromJsonString(
            hostLoginResult.getResponse().getContentAsString(), LoginResponse.class).getAccessToken();

        // Host can view guests but cannot delete
        mockMvc.perform(get("/api/guests/{id}", testGuest.getId())
                .header("Authorization", hostToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/guests/{id}", testGuest.getId())
                .header("Authorization", hostToken))
                .andExpect(status().isForbidden());

        // Test Server access
        LoginRequest serverLogin = TestDataBuilder.createLoginRequest(
            serverStaff.getEmail(), "password123");
        MvcResult serverLoginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(serverLogin)))
                .andExpect(status().isOk())
                .andReturn();
        String serverToken = "Bearer " + fromJsonString(
            serverLoginResult.getResponse().getContentAsString(), LoginResponse.class).getAccessToken();

        // Server can create visits
        VisitCreateRequest visitRequest = TestDataBuilder.createVisitCreateRequest(
            testGuest.getId(), serverStaff.getId());

        mockMvc.perform(post("/api/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(visitRequest))
                .header("Authorization", serverToken))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Error handling workflow: Invalid data -> Validation errors -> Recovery")
    void errorHandlingWorkflow() throws Exception {
        // Step 1: Login
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            hostStaff.getEmail(), "password123");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String authToken = "Bearer " + fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class).getAccessToken();

        // Step 2: Try to create guest with invalid data
        GuestCreateRequest invalidRequest = new GuestCreateRequest();
        invalidRequest.setFirstName(""); // Empty name
        invalidRequest.setPhone("invalid-phone"); // Invalid phone format

        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // Step 3: Create valid guest
        GuestCreateRequest validRequest = TestDataBuilder.createGuestCreateRequest();
        MvcResult guestResult = mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validRequest))
                .header("Authorization", authToken))
                .andExpect(status().isCreated())
                .andReturn();

        GuestResponse createdGuest = fromJsonString(
            guestResult.getResponse().getContentAsString(), GuestResponse.class);

        // Step 4: Try to create duplicate guest
        mockMvc.perform(post("/api/guests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validRequest))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        // Step 5: Try to access non-existent guest
        mockMvc.perform(get("/api/guests/{id}", 99999L)
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());

        // Step 6: Successfully access existing guest
        mockMvc.perform(get("/api/guests/{id}", createdGuest.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdGuest.getId()));
    }
}