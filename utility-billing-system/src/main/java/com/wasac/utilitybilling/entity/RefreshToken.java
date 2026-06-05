package com.wasac.utilitybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {
    @Column(nullable = false, unique = true, length = 120)
    private String token;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false)
    private Instant expiresAt;
    private Instant revokedAt;
}
