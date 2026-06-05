package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.PenaltyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "penalties")
public class Penalty extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyType type;
    @Column(name = "penalty_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal value;
    @Column(nullable = false)
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable = false)
    private int version;
    @Column(nullable = false)
    private boolean active = true;
}
