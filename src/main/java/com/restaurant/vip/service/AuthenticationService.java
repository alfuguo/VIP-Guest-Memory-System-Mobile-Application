package com.restaurant.vip.service;

import com.restaurant.vip.dto.LoginRequest;
import com.restaurant.vip.dto.LoginResponse;
import com.restaurant.vip.dto.RefreshTokenRequest;
import com.restaurant.vip.dto.StaffProfileResponse;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.exception.AccountLockedException;
import com.restaurant.vip.exception.InvalidCredentialsException;
import com.restaurant.vip.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Value("${security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${security.lockout-duration}")
    private long lockoutDuration;

    public LoginResponse authenticate(LoginRequest request) {
        Staff staff = staffRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check if account is active
        if (!staff.getActive()) {
            auditLogService.logFailedLogin(staff, "Account is deactivated");
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Check if account is locked
        if (staff.isAccountLocked()) {
            auditLogService.logFailedLogin(staff, "Account is locked");
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts");
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Reset failed login attempts on successful authentication
            sessionManagementService.resetFailedLoginAttempts(staff);

            // Generate tokens
            String accessToken = jwtService.generateToken(staff);
            String refreshToken = jwtService.generateRefreshToken(staff);

            // Create session
            sessionManagementService.createSession(accessToken, staff);

            // Log successful login
            auditLogService.logSuccessfulLogin(staff);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900) // 15 minutes
                    .staffId(staff.getId())
                    .email(staff.getEmail())
                    .firstName(staff.getFirstName())
                    .lastName(staff.getLastName())
                    .role(staff.getRole())
                    .build();

        } catch (AuthenticationException e) {
            // Handle failed authentication
            sessionManagementService.handleFailedLoginAttempt(request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        try {
            String userEmail = jwtService.extractUsername(refreshToken);
            Staff staff = staffRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

            if (!staff.getActive() || staff.isAccountLocked()) {
                throw new InvalidCredentialsException("Account is not active");
            }

            if (!jwtService.isTokenExpired(refreshToken)) {
                String newAccessToken = jwtService.generateToken(staff);
                String newRefreshToken = jwtService.generateRefreshToken(staff);

                // Update session with new token
                sessionManagementService.removeSession(refreshToken);
                sessionManagementService.createSession(newAccessToken, staff);

                auditLogService.logTokenRefresh(staff);

                return LoginResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType("Bearer")
                        .expiresIn(900) // 15 minutes
                        .staffId(staff.getId())
                        .email(staff.getEmail())
                        .firstName(staff.getFirstName())
                        .lastName(staff.getLastName())
                        .role(staff.getRole())
                        .build();
            } else {
                throw new InvalidCredentialsException("Refresh token is expired");
            }
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }



    public void logout(String token) {
        try {
            String userEmail = jwtService.extractUsername(token);
            Staff staff = staffRepository.findByEmail(userEmail).orElse(null);
            
            if (staff != null) {
                auditLogService.logLogout(staff);
            }
            
            // Remove session
            sessionManagementService.removeSession(token);
            
        } catch (Exception e) {
            // Token might be invalid, but we don't throw an exception for logout
        }
    }

    public StaffProfileResponse getProfile(String token) {
        try {
            String userEmail = jwtService.extractUsername(token);
            Staff staff = staffRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid token"));

            if (!staff.getActive() || staff.isAccountLocked()) {
                throw new InvalidCredentialsException("Account is not active");
            }

            return new StaffProfileResponse(
                    staff.getId(),
                    staff.getEmail(),
                    staff.getFirstName(),
                    staff.getLastName(),
                    staff.getRole(),
                    staff.getActive(),
                    staff.getCreatedAt()
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }

    /**
     * Find staff member by email
     */
    public Staff findByEmail(String email) {
        return staffRepository.findByEmail(email).orElse(null);
    }
}