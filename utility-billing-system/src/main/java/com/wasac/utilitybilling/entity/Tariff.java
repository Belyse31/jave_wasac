package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.MeterType;
import com.wasac.utilitybilling.entity.enums.TariffModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tariffs")
public class Tariff extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeterType meterType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TariffModel model;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal rate;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fixedCharge = BigDecimal.ZERO;
    @Column(nullable = false)
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @Column(nullable = false)
    private int version;
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffTier> tiers = new ArrayList<>();
}
