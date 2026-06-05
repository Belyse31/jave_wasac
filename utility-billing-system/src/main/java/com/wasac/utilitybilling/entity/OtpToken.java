package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "otp_tokens")
public class OtpToken extends BaseEntity {
    @Column(nullable = false)
    private String email;
    @Column(nullable = false, length = 10)
    private String otp;
    @Column(nullable = false, unique = true, length = 80)
    private String verificationToken;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType purpose;
    @Column(nullable = false)
    private Instant expiresAt;
    private Instant usedAt;
}
