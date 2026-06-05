package com.wasac.utilitybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "taxes")
public class Tax extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal rate;
    @Column(nullable = false)
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable = false)
    private int version;
    @Column(nullable = false)
    private boolean active = true;
}
