package com.wasac.utilitybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tariff_tiers")
public class TariffTier extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;
    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal fromUnits;
    @Column(precision = 18, scale = 3)
    private BigDecimal toUnits;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal rate;
}
