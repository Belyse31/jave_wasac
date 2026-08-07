package com.wasac.utilitybilling.service;

public interface EmailService {
    void sendWelcomeEmail(String to, String fullName);
    void sendOtpEmail(String to, String otp);
    void sendVerificationEmail(String to, String verificationLink);
    void sendPasswordResetEmail(String to, String otp);
    void sendStaffCredentialsEmail(String to, String fullName, String role, String temporaryPassword);
    void sendBillNotificationEmail(String to, String monthYear, String amount);
    void sendApprovedBillEmail(String to, String fullName, String billReference, String monthYear, String amount, String dueDate);
    void sendPaymentConfirmationEmail(String to, String reference, String amount, String remainingBalance, String billStatus);
}
