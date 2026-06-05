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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    private String recipientEmail;
    private String recipientPhone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @Column(nullable = false)
    private String subject;
    @Column(nullable = false, length = 2000)
    private String message;
    @Column(nullable = false)
    private boolean sent;
    @Column(unique = true)
    private String relatedReference;
}
