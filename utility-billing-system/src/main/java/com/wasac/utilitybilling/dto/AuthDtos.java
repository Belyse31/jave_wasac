package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @Schema(example = "Belyse Uwase")
            @NotBlank(message = "Full name is required")
            @Pattern(regexp = "^[^@]+$", message = "Full name cannot be an email")
            String fullName,
            @Schema(example = "customer@gmail.com")
            @NotBlank(message = "Email is required")
            @Email(message = "Email format is invalid")
            @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be valid and lowercase")
            String email,
            @Schema(example = "0781234567")
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = ValidationPatterns.RWANDA_PHONE, message = "Phone number must be a valid Rwanda mobile number")
            String phoneNumber,
            @Schema(example = "Password@123")
            @NotBlank(message = "Password is required")
            @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD, message = "Password must have at least 8 characters, uppercase, lowercase, number, and special character")
            String password
    ) {
    }

    public record LoginRequest(
            @Schema(example = "belyse457@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be lowercase") String email,
            @Schema(example = "Admin@12345")
            @NotBlank String password
    ) {
    }

    public record TokenRefreshRequest(@Schema(example = "refresh-token-from-login-response") @NotBlank String refreshToken) {
    }

    public record VerifyOtpRequest(
            @Schema(example = "customer@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be lowercase") String email,
            @Schema(example = "123456")
            @NotBlank @Size(min = 6, max = 6, message = "OTP must be 6 digits") @Pattern(regexp = "\\d{6}") String otp
    ) {
    }

    public record ForgotPasswordRequest(
            @Schema(example = "customer@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be lowercase") String email
    ) {
    }

    public record ResetPasswordRequest(
            @Schema(example = "customer@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be lowercase") String email,
            @Schema(example = "123456")
            @NotBlank @Size(min = 6, max = 6) @Pattern(regexp = "\\d{6}") String otp,
            @Schema(example = "NewPassword@123")
            @NotBlank @Pattern(regexp = ValidationPatterns.STRONG_PASSWORD, message = "Password must have at least 8 characters, uppercase, lowercase, number, and special character") String newPassword
    ) {
    }

    public record AuthResponse(UUID userId, String fullName, String email, Set<Role> roles, boolean passwordChangeRequired, String accessToken, String refreshToken) {
    }
}
