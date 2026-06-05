package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.BillStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bill_meter_month", columnNames = {"meter_id", "billing_year", "billing_month"})
})
public class Bill extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String billReference;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne(optional = false)
    @JoinColumn(name = "meter_id")
    private Meter meter;
    @OneToOne(optional = false)
    @JoinColumn(name = "reading_id")
    private MeterReading reading;
    @Column(nullable = false)
    private int billingYear;
    @Column(nullable = false)
    private int billingMonth;
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal consumption;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal consumptionCharge;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fixedCharge;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal penaltyAmount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingBalance;
    @Column(nullable = false)
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillStatus status = BillStatus.PENDING;
    private boolean approved;
    private Instant latePenaltyAppliedAt;
}
