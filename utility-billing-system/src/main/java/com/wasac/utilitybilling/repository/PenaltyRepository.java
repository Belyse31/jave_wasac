package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PenaltyRepository extends JpaRepository<Penalty, UUID> {
    List<Penalty> findByActiveIsTrueAndEffectiveFromLessThanEqualAndEffectiveToIsNull(LocalDate date);
    @Query("""
            select p from Penalty p
            where p.effectiveFrom <= :billingDate
              and (p.effectiveTo is null or p.effectiveTo >= :billingDate)
            order by p.name asc, p.effectiveFrom desc, p.version desc
            """)
    List<Penalty> findEffectivePenalties(@Param("billingDate") LocalDate billingDate);

    Optional<Penalty> findTopByNameIgnoreCaseOrderByVersionDesc(String name);
}
