package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.CustomerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true, length = 32)
    private String nationalId;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true, length = 32)
    private String phoneNumber;
    @Column(nullable = false)
    private String address;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;
}
