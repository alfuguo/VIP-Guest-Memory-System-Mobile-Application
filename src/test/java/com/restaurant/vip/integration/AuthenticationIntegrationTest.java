package com.restaurant.vip.integration;

import com.restaurant.vip.dto.LoginRequest;
import com.restaurant.vip.dto.LoginResponse;
import com.restaurant.vip.dto.RefreshTokenRequest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.repository.StaffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints.
 * Tests the complete authentication flow including login, token refresh, and logout.
 */
@AutoConfigureWebMvc
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private StaffRepository staffRepository;

    private Staff testStaff;

    @Override
    protected void setupTestData() {
        // Create test staff member
        testStaff = TestDataBuilder.createManagerStaff();
        testStaff = staffRepository.save(testStaff);
    }

    @Test
    @DisplayName("Should successfully authenticate valid user credentials")
    void shouldAuthenticateValidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.staff.email").value(testStaff.getEmail()))
                .andExpect(jsonPath("$.staff.role").value("MANAGER"))
                .andReturn();

        // Verify response structure
        String responseJson = result.getResponse().getContentAsString();
        LoginResponse response = fromJsonString(responseJson, LoginResponse.class);
        
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getStaff().getEmail()).isEqualTo(testStaff.getEmail());
        assertThat(response.getStaff().getRole().toString()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "wrongpassword");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should reject login for non-existent user")
    void shouldRejectNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            "nonexistent@restaurant.com", "password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should reject login for inactive user")
    void shouldRejectInactiveUser() throws Exception {
        // Given - deactivate the test staff
        testStaff.setActive(false);
        staffRepository.save(testStaff);

        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should validate required fields in login request")
    void shouldValidateLoginRequestFields() throws Exception {
        // Given - empty login request
        LoginRequest loginRequest = new LoginRequest();

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should refresh token with valid refresh token")
    void shouldRefreshTokenWithValidRefreshToken() throws Exception {
        // Given - first login to get tokens
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);

        // When - refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(loginResponse.getRefreshToken());

        // Then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Should reject refresh with invalid token")
    void shouldRejectInvalidRefreshToken() throws Exception {
        // Given
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should successfully logout with valid token")
    void shouldLogoutWithValidToken() throws Exception {
        // Given - first login to get token
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);

        // When & Then
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get user profile with valid token")
    void shouldGetProfileWithValidToken() throws Exception {
        // Given - first login to get token
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = fromJsonString(
            loginResult.getResponse().getContentAsString(), LoginResponse.class);

        // When & Then
        mockMvc.perform(get("/auth/profile")
                .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testStaff.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testStaff.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testStaff.getLastName()))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    @DisplayName("Should reject profile request without token")
    void shouldRejectProfileRequestWithoutToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject profile request with invalid token")
    void shouldRejectProfileRequestWithInvalidToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/profile")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle concurrent login attempts")
    void shouldHandleConcurrentLoginAttempts() throws Exception {
        // Given
        LoginRequest loginRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "password123");

        // When - simulate concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists());
        }
    }

    @Test
    @DisplayName("Should enforce account lockout after failed attempts")
    void shouldEnforceAccountLockout() throws Exception {
        // Given
        LoginRequest invalidRequest = TestDataBuilder.createLoginRequest(
            testStaff.getEmail(), "wrongpassword");

        // When - make multiple failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(invalidRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // Then - next attempt should be locked
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("locked")));
    }
}