package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.AuthDtos;

public interface AuthService {
    void register(AuthDtos.RegisterRequest request, String ipAddress);
    AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, String ipAddress);
    AuthDtos.AuthResponse refresh(AuthDtos.TokenRefreshRequest request);
    void logout(AuthDtos.TokenRefreshRequest request, String username, String ipAddress);
    void verifyOtp(AuthDtos.VerifyOtpRequest request);
    void verifyEmail(String token);
    void resendOtp(String email);
    void forgotPassword(AuthDtos.ForgotPasswordRequest request);
    void resetPassword(AuthDtos.ResetPasswordRequest request);
}
