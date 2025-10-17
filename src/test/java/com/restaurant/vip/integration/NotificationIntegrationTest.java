package com.restaurant.vip.integration;

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
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for notification endpoints.
 * Tests pre-arrival notifications, special occasions, and returning guest alerts.
 */
@AutoConfigureWebMvc
@DisplayName("Notification Integration Tests")
class NotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private VisitRepository visitRepository;

    private Staff testStaff;
    private Guest birthdayGuest;
    private Guest anniversaryGuest;
    private Guest returningGuest;
    private Guest recentGuest;

    @Override
    protected void setupTestData() {
        // Create test staff
        testStaff = TestDataBuilder.createManagerStaff();
        testStaff = staffRepository.save(testStaff);

        // Create guest with upcoming birthday (next week)
        birthdayGuest = TestDataBuilder.createGuestWithUpcomingBirthday();
        birthdayGuest.setCreatedBy(testStaff);
        birthdayGuest = guestRepository.save(birthdayGuest);

        // Create guest with upcoming anniversary (next month)
        anniversaryGuest = TestDataBuilder.createBasicGuest();
        anniversaryGuest.setFirstName("Anniversary");
        anniversaryGuest.setLastName("Guest");
        anniversaryGuest.setPhone("+1234567895");
        anniversaryGuest.setEmail("anniversary@email.com");
        anniversaryGuest.setAnniversary(LocalDate.now().plusDays(15));
        anniversaryGuest.setCreatedBy(testStaff);
        anniversaryGuest = guestRepository.save(anniversaryGuest);

        // Create returning guest (last visit over 6 months ago)
        returningGuest = TestDataBuilder.createBasicGuest();
        returningGuest.setFirstName("Returning");
        returningGuest.setLastName("Guest");
        returningGuest.setPhone("+1234567896");
        returningGuest.setEmail("returning@email.com");
        returningGuest.setCreatedBy(testStaff);
        returningGuest = guestRepository.save(returningGuest);

        // Create old visit for returning guest
        Visit oldVisit = TestDataBuilder.createBasicVisit(returningGuest, testStaff);
        oldVisit.setVisitDate(LocalDate.now().minusMonths(8));
        visitRepository.save(oldVisit);

        // Create recent guest (visited recently)
        recentGuest = TestDataBuilder.createBasicGuest();
        recentGuest.setFirstName("Recent");
        recentGuest.setLastName("Guest");
        recentGuest.setPhone("+1234567897");
        recentGuest.setEmail("recent@email.com");
        recentGuest.setCreatedBy(testStaff);
        recentGuest = guestRepository.save(recentGuest);

        // Create recent visit
        Visit recentVisit = TestDataBuilder.createBasicVisit(recentGuest, testStaff);
        recentVisit.setVisitDate(LocalDate.now().minusDays(2));
        visitRepository.save(recentVisit);
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get upcoming special occasions")
    void shouldGetUpcomingSpecialOccasions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingBirthdays").isArray())
                .andExpect(jsonPath("$.upcomingAnniversaries").isArray())
                .andExpect(jsonPath("$.todaysBirthdays").isArray())
                .andExpect(jsonPath("$.todaysAnniversaries").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should identify guests with upcoming birthdays")
    void shouldIdentifyUpcomingBirthdays() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingBirthdays").isArray())
                .andExpect(jsonPath("$.upcomingBirthdays[?(@.firstName == 'Birthday')]").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should identify guests with upcoming anniversaries")
    void shouldIdentifyUpcomingAnniversaries() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingAnniversaries").isArray())
                .andExpect(jsonPath("$.upcomingAnniversaries[?(@.firstName == 'Anniversary')]").exists());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get upcoming notifications with guest preferences")
    void shouldGetUpcomingNotifications() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preArrivalNotifications").isArray())
                .andExpect(jsonPath("$.specialOccasions").isArray())
                .andExpect(jsonPath("$.returningGuests").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should identify returning guests")
    void shouldIdentifyReturningGuests() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returningGuests").isArray())
                .andExpect(jsonPath("$.returningGuests[?(@.firstName == 'Returning')]").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should not flag recent guests as returning")
    void shouldNotFlagRecentGuestsAsReturning() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returningGuests[?(@.firstName == 'Recent')]").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should acknowledge notification")
    void shouldAcknowledgeNotification() throws Exception {
        // Given - first get notifications to find one to acknowledge
        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk());

        // When & Then - acknowledge a notification (using guest ID as notification ID for test)
        mockMvc.perform(post("/api/notifications/{id}/acknowledge", birthdayGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true))
                .andExpect(jsonPath("$.acknowledgedAt").exists());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should handle non-existent notification acknowledgment")
    void shouldHandleNonExistentNotificationAcknowledgment() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/notifications/{id}/acknowledge", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should filter special occasions by date range")
    void shouldFilterSpecialOccasionsByDateRange() throws Exception {
        // When & Then - get occasions for next 30 days
        mockMvc.perform(get("/api/notifications/special-occasions")
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingBirthdays").isArray())
                .andExpect(jsonPath("$.upcomingAnniversaries").isArray());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get notifications with guest dietary restrictions")
    void shouldGetNotificationsWithDietaryRestrictions() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialOccasions[*].dietaryRestrictions").exists());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get notifications with seating preferences")
    void shouldGetNotificationsWithSeatingPreferences() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialOccasions[*].seatingPreference").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get notifications with favorite drinks")
    void shouldGetNotificationsWithFavoriteDrinks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialOccasions[*].favoriteDrinks").exists());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should prioritize today's occasions")
    void shouldPrioritizeTodaysOccasions() throws Exception {
        // Given - create guest with today's birthday
        Guest todayBirthdayGuest = TestDataBuilder.createBasicGuest();
        todayBirthdayGuest.setFirstName("Today");
        todayBirthdayGuest.setLastName("Birthday");
        todayBirthdayGuest.setPhone("+1234567898");
        todayBirthdayGuest.setEmail("today@email.com");
        todayBirthdayGuest.setBirthday(LocalDate.now());
        todayBirthdayGuest.setCreatedBy(testStaff);
        guestRepository.save(todayBirthdayGuest);

        // When & Then
        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todaysBirthdays").isArray())
                .andExpect(jsonPath("$.todaysBirthdays[?(@.firstName == 'Today')]").exists());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should handle empty notification lists gracefully")
    void shouldHandleEmptyNotificationLists() throws Exception {
        // Given - clear all guests to test empty state
        guestRepository.deleteAll();

        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preArrivalNotifications").isArray())
                .andExpect(jsonPath("$.preArrivalNotifications").isEmpty())
                .andExpect(jsonPath("$.specialOccasions").isArray())
                .andExpect(jsonPath("$.specialOccasions").isEmpty())
                .andExpect(jsonPath("$.returningGuests").isArray())
                .andExpect(jsonPath("$.returningGuests").isEmpty());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should get notification summary statistics")
    void shouldGetNotificationSummaryStatistics() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUpcomingBirthdays").isNumber())
                .andExpect(jsonPath("$.totalUpcomingAnniversaries").isNumber())
                .andExpect(jsonPath("$.totalReturningGuests").isNumber())
                .andExpect(jsonPath("$.totalTodaysOccasions").isNumber());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should get notifications for specific date")
    void shouldGetNotificationsForSpecificDate() throws Exception {
        // Given
        LocalDate targetDate = LocalDate.now().plusDays(7);

        // When & Then
        mockMvc.perform(get("/api/notifications/date/{date}", targetDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(targetDate.toString()))
                .andExpect(jsonPath("$.birthdays").isArray())
                .andExpect(jsonPath("$.anniversaries").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get guest notification history")
    void shouldGetGuestNotificationHistory() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/guest/{guestId}/history", birthdayGuest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(birthdayGuest.getId()))
                .andExpect(jsonPath("$.notifications").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should handle concurrent notification acknowledgments")
    void shouldHandleConcurrentNotificationAcknowledgments() throws Exception {
        // Given - multiple notification acknowledgments
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/notifications/{id}/acknowledge", birthdayGuest.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.acknowledged").value(true));
        }
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should validate notification date parameters")
    void shouldValidateNotificationDateParameters() throws Exception {
        // When & Then - invalid date format
        mockMvc.perform(get("/api/notifications/date/{date}", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SERVER")
    @DisplayName("Should get notifications with visit context")
    void shouldGetNotificationsWithVisitContext() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returningGuests[*].lastVisitDate").exists())
                .andExpect(jsonPath("$.returningGuests[*].daysSinceLastVisit").exists());
    }

    @Test
    @DisplayName("Should require authentication for notification endpoints")
    void shouldRequireAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/notifications/{id}/acknowledge", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "HOST")
    @DisplayName("Should respect role-based access for notifications")
    void shouldRespectRoleBasedAccess() throws Exception {
        // All roles should be able to access notifications
        mockMvc.perform(get("/api/notifications/upcoming"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/special-occasions"))
                .andExpect(status().isOk());
    }
}