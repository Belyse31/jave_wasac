package com.wasac.utilitybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "meter_readings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_meter_reading_month", columnNames = {"meter_id", "billing_year", "billing_month"})
})
public class MeterReading extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "meter_id")
    private Meter meter;
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal previousReading;
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal currentReading;
    @Column(nullable = false)
    private LocalDate readingDate;
    @Column(name = "billing_year", nullable = false)
    private int billingYear;
    @Column(name = "billing_month", nullable = false)
    private int billingMonth;
}
