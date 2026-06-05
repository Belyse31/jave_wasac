package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.OtpToken;
import com.wasac.utilitybilling.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findTopByEmailAndOtpAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(String email, String otp, NotificationType purpose);
    Optional<OtpToken> findByVerificationTokenAndUsedAtIsNull(String verificationToken);
}
