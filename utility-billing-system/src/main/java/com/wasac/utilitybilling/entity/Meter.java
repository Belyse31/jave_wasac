package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.entity.enums.MeterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "meters")
public class Meter extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String meterNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;
    @Column(nullable = false)
    private LocalDate installationDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterStatus status = MeterStatus.ACTIVE;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
