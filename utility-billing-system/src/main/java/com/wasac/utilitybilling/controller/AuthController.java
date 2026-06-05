package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.AuthDtos;
import com.wasac.utilitybilling.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Public: register a customer account and send OTP/email verification")
    ApiResponse<Void> register(@Valid @RequestBody AuthDtos.RegisterRequest request, HttpServletRequest servletRequest) {
        authService.register(request, servletRequest.getRemoteAddr());
        return ApiResponse.ok("Registration successful. Verify the OTP sent to your email.", null);
    }

    @PostMapping("/login")
    @Operation(summary = "Public: login and receive JWT access and refresh tokens")
    ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.ok("Login successful", authService.login(request, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Authenticated: refresh JWT access token")
    ApiResponse<AuthDtos.AuthResponse> refresh(@Valid @RequestBody AuthDtos.TokenRefreshRequest request) {
        return ApiResponse.ok("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Authenticated: logout and revoke refresh token")
    ApiResponse<Void> logout(@Valid @RequestBody AuthDtos.TokenRefreshRequest request, Principal principal, HttpServletRequest servletRequest) {
        authService.logout(request, principal.getName(), servletRequest.getRemoteAddr());
        return ApiResponse.ok("Logout successful", null);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Public: verify 6-digit OTP and activate account")
    ApiResponse<Void> verifyOtp(@Valid @RequestBody AuthDtos.VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ApiResponse.ok("Account verified successfully", null);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Public: verify account through email verification link")
    ApiResponse<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ApiResponse.ok("Email verified successfully", null);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Public: resend account verification OTP")
    ApiResponse<Void> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ApiResponse.ok("OTP resent", null);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Public: request password reset OTP")
    ApiResponse<Void> forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok("Password reset OTP sent", null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Public: reset password using OTP")
    ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset successful", null);
    }
}
