package com.restaurant.vip.service;

import com.restaurant.vip.dto.LoginRequest;
import com.restaurant.vip.dto.LoginResponse;
import com.restaurant.vip.dto.RefreshTokenRequest;
import com.restaurant.vip.dto.StaffProfileResponse;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.StaffRole;
import com.restaurant.vip.exception.AccountLockedException;
import com.restaurant.vip.exception.InvalidCredentialsException;
import com.restaurant.vip.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SessionManagementService sessionManagementService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Staff testStaff;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        // Setup test staff
        testStaff = new Staff();
        testStaff.setId(1L);
        testStaff.setEmail("test@restaurant.com");
        testStaff.setFirstName("John");
        testStaff.setLastName("Doe");
        testStaff.setRole(StaffRole.SERVER);
        testStaff.setActive(true);
        testStaff.setFailedLoginAttempts(0);
        testStaff.setAccountLockedUntil(null);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@restaurant.com");
        loginRequest.setPassword("password123");

        // Setup refresh token request
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");

        // Set configuration values
        ReflectionTestUtils.setField(authenticationService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authenticationService, "lockoutDuration", 1800000L); // 30 minutes
    }

    @Test
    void authenticate_Success() {
        // Arrange
        when(staffRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testStaff));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(testStaff)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testStaff)).thenReturn("refresh-token");

        // Act
        LoginResponse result = authenticationService.authenticate(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(900, result.getExpiresIn());
        assertEquals(testStaff.getId(), result.getStaffId());
        assertEquals(testStaff.getEmail(), result.getEmail());
        assertEquals(testStaff.getRole(), result.getRole());

        verify(staffRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(sessionManagementService).resetFailedLoginAttempts(testStaff);
        verify(sessionManagementService).createSession("access-token", testStaff);
        verify(auditLogService).logSuccessfulLogin(testStaff);
    }

    @Test
    void authenticate_InvalidEmail_ThrowsException() {
        // Arrange
        when(staffRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(loginRequest));

        verify(staffRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_InactiveAccount_ThrowsException() {
        // Arrange
        testStaff.setActive(false);
        when(staffRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testStaff));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(loginRequest));

        verify(staffRepository).findByEmail(loginRequest.getEmail());
        verify(auditLogService).logFailedLogin(testStaff, "Account is deactivated");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_LockedAccount_ThrowsException() {
        // Arrange
        testStaff.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(staffRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testStaff));

        // Act & Assert
        assertThrows(AccountLockedException.class, () -> authenticationService.authenticate(loginRequest));

        verify(staffRepository).findByEmail(loginRequest.getEmail());
        verify(auditLogService).logFailedLogin(testStaff, "Account is locked");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void authenticate_BadCredentials_ThrowsException() {
        // Arrange
        when(staffRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testStaff));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(loginRequest));

        verify(staffRepository).findByEmail(loginRequest.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(sessionManagementService).handleFailedLoginAttempt(loginRequest.getEmail());
        verify(sessionManagementService, never()).createSession(anyString(), any(Staff.class));
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        when(jwtService.extractUsername("refresh-token")).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);
        when(jwtService.generateToken(testStaff)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testStaff)).thenReturn("new-refresh-token");

        // Act
        LoginResponse result = authenticationService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        assertEquals(testStaff.getId(), result.getStaffId());

        verify(jwtService).extractUsername("refresh-token");
        verify(staffRepository).findByEmail("test@restaurant.com");
        verify(jwtService).isTokenExpired("refresh-token");
        verify(sessionManagementService).removeSession("refresh-token");
        verify(sessionManagementService).createSession("new-access-token", testStaff);
        verify(auditLogService).logTokenRefresh(testStaff);
    }

    @Test
    void refreshToken_InvalidToken_ThrowsException() {
        // Arrange
        when(jwtService.extractUsername("refresh-token")).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUsername("refresh-token");
        verify(staffRepository, never()).findByEmail(anyString());
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsException() {
        // Arrange
        when(jwtService.extractUsername("refresh-token")).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUsername("refresh-token");
        verify(staffRepository).findByEmail("test@restaurant.com");
        verify(jwtService).isTokenExpired("refresh-token");
        verify(sessionManagementService, never()).createSession(anyString(), any(Staff.class));
    }

    @Test
    void refreshToken_InactiveStaff_ThrowsException() {
        // Arrange
        testStaff.setActive(false);
        when(jwtService.extractUsername("refresh-token")).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.refreshToken(refreshTokenRequest));

        verify(jwtService).extractUsername("refresh-token");
        verify(staffRepository).findByEmail("test@restaurant.com");
        verify(jwtService, never()).isTokenExpired(anyString());
    }

    @Test
    void logout_Success() {
        // Arrange
        String token = "access-token";
        when(jwtService.extractUsername(token)).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act
        authenticationService.logout(token);

        // Assert
        verify(jwtService).extractUsername(token);
        verify(staffRepository).findByEmail("test@restaurant.com");
        verify(auditLogService).logLogout(testStaff);
        verify(sessionManagementService).removeSession(token);
    }

    @Test
    void logout_InvalidToken_DoesNotThrowException() {
        // Arrange
        String token = "invalid-token";
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertDoesNotThrow(() -> authenticationService.logout(token));

        verify(jwtService).extractUsername(token);
        verify(sessionManagementService).removeSession(token);
        verify(auditLogService, never()).logLogout(any(Staff.class));
    }

    @Test
    void getProfile_Success() {
        // Arrange
        String token = "access-token";
        when(jwtService.extractUsername(token)).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act
        StaffProfileResponse result = authenticationService.getProfile(token);

        // Assert
        assertNotNull(result);
        assertEquals(testStaff.getId(), result.getId());
        assertEquals(testStaff.getEmail(), result.getEmail());
        assertEquals(testStaff.getFirstName(), result.getFirstName());
        assertEquals(testStaff.getLastName(), result.getLastName());
        assertEquals(testStaff.getRole(), result.getRole());
        assertEquals(testStaff.getActive(), result.getActive());

        verify(jwtService).extractUsername(token);
        verify(staffRepository).findByEmail("test@restaurant.com");
    }

    @Test
    void getProfile_InvalidToken_ThrowsException() {
        // Arrange
        String token = "invalid-token";
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.getProfile(token));

        verify(jwtService).extractUsername(token);
        verify(staffRepository, never()).findByEmail(anyString());
    }

    @Test
    void getProfile_StaffNotFound_ThrowsException() {
        // Arrange
        String token = "access-token";
        when(jwtService.extractUsername(token)).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.getProfile(token));

        verify(jwtService).extractUsername(token);
        verify(staffRepository).findByEmail("test@restaurant.com");
    }

    @Test
    void getProfile_InactiveStaff_ThrowsException() {
        // Arrange
        String token = "access-token";
        testStaff.setActive(false);
        when(jwtService.extractUsername(token)).thenReturn("test@restaurant.com");
        when(staffRepository.findByEmail("test@restaurant.com")).thenReturn(Optional.of(testStaff));

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authenticationService.getProfile(token));

        verify(jwtService).extractUsername(token);
        verify(staffRepository).findByEmail("test@restaurant.com");
    }
}