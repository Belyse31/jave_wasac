package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxRepository extends JpaRepository<Tax, UUID> {
    List<Tax> findByActiveIsTrueAndEffectiveFromLessThanEqualAndEffectiveToIsNull(LocalDate date);
    @Query("""
            select t from Tax t
            where t.effectiveFrom <= :billingDate
              and (t.effectiveTo is null or t.effectiveTo >= :billingDate)
            order by t.name asc, t.effectiveFrom desc, t.version desc
            """)
    List<Tax> findEffectiveTaxes(@Param("billingDate") LocalDate billingDate);

    Optional<Tax> findTopByNameIgnoreCaseOrderByVersionDesc(String name);
}
