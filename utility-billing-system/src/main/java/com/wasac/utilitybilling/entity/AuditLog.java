package com.wasac.utilitybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {
    private String username;
    @Column(nullable = false)
    private String action;
    private String ipAddress;
    @Column(length = 3000)
    private String details;
}
