package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.AuthDtos;
import com.wasac.utilitybilling.entity.OtpToken;
import com.wasac.utilitybilling.entity.RefreshToken;
import com.wasac.utilitybilling.entity.User;
import com.wasac.utilitybilling.entity.enums.AccountStatus;
import com.wasac.utilitybilling.entity.enums.NotificationType;
import com.wasac.utilitybilling.entity.enums.Role;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.InvalidOtpException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.OtpTokenRepository;
import com.wasac.utilitybilling.repository.RefreshTokenRepository;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.security.JwtService;
import com.wasac.utilitybilling.service.AuditService;
import com.wasac.utilitybilling.service.AuthService;
import com.wasac.utilitybilling.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.security.public-base-url}")
    private String publicBaseUrl;
    @Value("${app.jwt.refresh-token-days}")
    private long refreshTokenDays;
    @Value("${app.otp.expiry-minutes}")
    private long otpExpiryMinutes;

    @Override
    @Transactional
    public void register(AuthDtos.RegisterRequest request, String ipAddress) {
        // Public registration is customer-only, so every registered user receives ROLE_CUSTOMER below.
        // Email uniqueness prevents duplicate login identities.
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        // Phone uniqueness prevents duplicate accounts and supports future recovery workflows.
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Phone number is already registered");
        }
        // Build the new inactive user account from validated request data.
        User user = new User();
        // Store the customer's full name exactly as submitted.
        user.setFullName(request.fullName());
        // Store only lowercase validated email addresses.
        user.setEmail(request.email());
        // Store the validated Rwanda mobile number.
        user.setPhoneNumber(request.phoneNumber());
        // Password is encrypted with BCrypt before persistence.
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Public registration always creates a customer account, not staff.
        user.getRoles().add(Role.ROLE_CUSTOMER);
        // Save the pending account before sending verification credentials.
        userRepository.save(user);
        // Generate a six-digit OTP and verification token for account activation.
        OtpToken otp = createOtp(request.email(), NotificationType.OTP_VERIFICATION);
        // Send one registration email only: the numeric OTP verification code.
        emailService.sendOtpEmail(request.email(), otp.getOtp());
        // Audit registration so account creation is traceable.
        auditService.record(request.email(), "REGISTRATION", ipAddress, "User registered and OTP issued");
    }

    @Override
    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, String ipAddress) {
        // Spring Security checks that the email exists, password is correct, and account is active.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        // Load the domain user so we can update login metadata and issue refresh tokens.
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // Temporary staff passwords must be changed before expiry.
        if (user.getPasswordExpiresAt() != null && user.getPasswordExpiresAt().isBefore(Instant.now())) {
            auditService.record(user.getEmail(), "FAILED_AUTHENTICATION", ipAddress, "Temporary password expired");
            throw new BusinessRuleException("Temporary password has expired. Ask an admin to reset it.");
        }
        // Store the successful login timestamp.
        user.setLastLoginAt(Instant.now());
        // Create a database refresh token for session renewal and logout revocation.
        RefreshToken refresh = newRefreshToken(user);
        // Load Spring Security user details for JWT generation.
        var details = userDetailsService.loadUserByUsername(user.getEmail());
        // Audit successful authentication.
        auditService.record(user.getEmail(), "LOGIN", ipAddress, "User login successful");
        // Return tokens and the password-change flag to the client.
        return new AuthDtos.AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRoles(), user.isPasswordChangeRequired(), jwtService.generate(details), refresh.getToken());
    }

    @Override
    @Transactional
    public AuthDtos.AuthResponse refresh(AuthDtos.TokenRefreshRequest request) {
        // Refresh token must exist in the database.
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));
        // Revoked or expired refresh tokens cannot issue new JWTs.
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Refresh token is expired or revoked");
        }
        // Rebuild user details so current roles are included in the new JWT.
        var details = userDetailsService.loadUserByUsername(token.getUser().getEmail());
        // Return a fresh access token with the same refresh token.
        return new AuthDtos.AuthResponse(token.getUser().getId(), token.getUser().getFullName(), token.getUser().getEmail(), token.getUser().getRoles(), token.getUser().isPasswordChangeRequired(), jwtService.generate(details), token.getToken());
    }

    @Override
    @Transactional
    public void logout(AuthDtos.TokenRefreshRequest request, String username, String ipAddress) {
        // Mark the refresh token revoked instead of deleting audit-relevant session history.
        refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
        // Log logout events for audit trail requirements.
        auditService.record(username, "LOGOUT", ipAddress, "User logout");
    }

    @Override
    @Transactional
    public void verifyOtp(AuthDtos.VerifyOtpRequest request) {
        // Validate and consume the OTP so it cannot be reused.
        OtpToken otp = consumeOtp(request.email(), request.otp(), NotificationType.OTP_VERIFICATION);
        // Activate the account after OTP verification succeeds.
        activateUser(otp.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        OtpToken otp = otpTokenRepository.findByVerificationTokenAndUsedAtIsNull(token)
                .orElseThrow(() -> new InvalidOtpException("Invalid verification token"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOtpException("Verification token has expired");
        }
        otp.setUsedAt(Instant.now());
        activateUser(otp.getEmail());
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getStatus() == AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account is already active");
        }
        OtpToken otp = createOtp(email, NotificationType.OTP_VERIFICATION);
        emailService.sendOtpEmail(email, otp.getOtp());
    }

    @Override
    public void forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        OtpToken otp = createOtp(request.email(), NotificationType.PASSWORD_RESET);
        emailService.sendPasswordResetEmail(request.email(), otp.getOtp());
    }

    @Override
    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        // Password reset requires a valid password-reset OTP.
        consumeOtp(request.email(), request.otp(), NotificationType.PASSWORD_RESET);
        // Load the user after OTP validation.
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // Store the new password as a BCrypt hash.
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        // Resetting password satisfies the first-login password-change requirement.
        user.setPasswordChangeRequired(false);
        // Clear temporary password expiry after successful reset.
        user.setPasswordExpiresAt(null);
    }

    private OtpToken createOtp(String email, NotificationType purpose) {
        // OTP record stores both numeric OTP and email-link token.
        OtpToken otp = new OtpToken();
        // Bind OTP to a specific email address.
        otp.setEmail(email);
        // Purpose separates verification OTPs from password-reset OTPs.
        otp.setPurpose(purpose);
        // Format random number as exactly six digits.
        otp.setOtp(String.format("%06d", random.nextInt(1_000_000)));
        // Token is used by the email verification link.
        otp.setVerificationToken(UUID.randomUUID().toString().replace("-", ""));
        // OTP expires after the configured number of minutes; default is 15 minutes.
        otp.setExpiresAt(Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES));
        // Save OTP so later verification can compare against database state.
        return otpTokenRepository.save(otp);
    }

    private OtpToken consumeOtp(String email, String code, NotificationType purpose) {
        // Find the latest unused OTP matching email, code, and purpose.
        OtpToken otp = otpTokenRepository.findTopByEmailAndOtpAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(email, code, purpose)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));
        // Expired OTPs are rejected even if the numeric code is correct.
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidOtpException("OTP has expired");
        }
        // Mark OTP as used to prevent replay attacks.
        otp.setUsedAt(Instant.now());
        // Return consumed OTP for the caller to use its email/purpose.
        return otp;
    }

    private void activateUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
    }

    private RefreshToken newRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString() + UUID.randomUUID());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        return refreshTokenRepository.save(refreshToken);
    }
}
