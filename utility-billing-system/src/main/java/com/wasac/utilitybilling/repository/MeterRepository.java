package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Meter;
import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.entity.enums.MeterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeterRepository extends JpaRepository<Meter, UUID> {
    boolean existsByMeterNumber(String meterNumber);
    Optional<Meter> findByMeterNumber(String meterNumber);
    Page<Meter> findByMeterNumberContainingIgnoreCase(String meterNumber, Pageable pageable);
    long countByMeterTypeAndStatus(MeterType meterType, MeterStatus status);
}
