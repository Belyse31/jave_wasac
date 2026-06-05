package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.PaymentMethod;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String paymentReference;
    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id")
    private Bill bill;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amountPaid;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    @Column(nullable = false)
    private LocalDate paymentDate;
}
