package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Tariff;
import com.wasac.utilitybilling.entity.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TariffRepository extends JpaRepository<Tariff, UUID> {
    @Query("""
            select t from Tariff t
            where t.meterType = :meterType
              and t.effectiveFrom <= :billingDate
              and (t.effectiveTo is null or t.effectiveTo >= :billingDate)
            order by t.effectiveFrom desc, t.version desc
            """)
    Optional<Tariff> findEffectiveTariff(@Param("meterType") MeterType meterType, @Param("billingDate") LocalDate billingDate);

    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);
}
