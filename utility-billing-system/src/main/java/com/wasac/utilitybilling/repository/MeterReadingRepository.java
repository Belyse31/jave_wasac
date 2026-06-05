package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Meter;
import com.wasac.utilitybilling.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterAndBillingYearAndBillingMonth(Meter meter, int billingYear, int billingMonth);
    Optional<MeterReading> findTopByMeterOrderByReadingDateDesc(Meter meter);
    List<MeterReading> findByBillingYearAndBillingMonth(int billingYear, int billingMonth);
}
