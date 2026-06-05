package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.entity.enums.NotificationType;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.service.EmailService;
import com.wasac.utilitybilling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final NotificationService notificationService;
    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendWelcomeEmail(String to, String fullName) {
        send(to, "Welcome to WASAC/REG Utility Billing", "Dear " + fullName + ",\n\nYour account has been created successfully.", NotificationType.REGISTRATION);
    }

    @Override
    public void sendOtpEmail(String to, String otp) {
        send(to, "WASAC/REG OTP Verification", "Dear customer,\n\nYour verification code is " + otp + ". It expires in 15 minutes.", NotificationType.OTP_VERIFICATION);
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        send(to, "Verify your WASAC/REG account", "Dear customer,\n\nPlease verify your account using this link: " + verificationLink, NotificationType.OTP_VERIFICATION);
    }

    @Override
    public void sendPasswordResetEmail(String to, String otp) {
        send(to, "WASAC/REG Password Reset", "Dear customer,\n\nUse this OTP to reset your password: " + otp, NotificationType.PASSWORD_RESET);
    }

    @Override
    public void sendStaffCredentialsEmail(String to, String fullName, String role, String temporaryPassword) {
        send(to, "WASAC/REG Staff Account Credentials",
                "Dear " + fullName + ",\n\nYour WASAC/REG staff account has been created.\n\nEmail: " + to
                        + "\nTemporary password: " + temporaryPassword
                        + "\nRole: " + role
                        + "\n\nPlease login and change your password immediately. The temporary password expires in 24 hours.",
                NotificationType.REGISTRATION);
    }

    @Override
    public void sendBillNotificationEmail(String to, String monthYear, String amount) {
        send(to, "WASAC/REG Utility Bill Processed", "Dear customer,\n\nYour " + monthYear + " utility bill of " + amount + " FRW has been successfully processed.", NotificationType.BILL_GENERATION);
    }

    @Override
    public void sendApprovedBillEmail(String to, String fullName, String billReference, String monthYear, String amount, String dueDate) {
        send(to, "WASAC/REG Approved Utility Bill",
                "Dear " + fullName + ",\n\nYour " + monthYear + " utility bill has been approved.\n\nBill reference: " + billReference
                        + "\nFinal amount to pay: " + amount + " FRW"
                        + "\nDue date: " + dueDate
                        + "\n\nPlease pay before the due date to avoid penalties.",
                NotificationType.BILL_GENERATION);
    }

    @Override
    public void sendPaymentConfirmationEmail(String to, String reference, String amount) {
        send(to, "WASAC/REG Payment Confirmation", "Dear customer,\n\nPayment " + reference + " of " + amount + " FRW has been received.", NotificationType.PAYMENT_CONFIRMATION);
    }

    private void send(String to, String subject, String body, NotificationType type) {
        boolean sent = false;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            sent = true;
        } catch (MailException ex) {
            throw new BusinessRuleException("Email could not be sent to " + to + ". Check the email address and SMTP configuration.");
        } finally {
            notificationService.create(to, null, type, subject, body, sent);
        }
    }
}
