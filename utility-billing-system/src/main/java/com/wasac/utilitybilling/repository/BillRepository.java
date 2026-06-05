package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Bill;
import com.wasac.utilitybilling.entity.Meter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    boolean existsByMeterAndBillingYearAndBillingMonth(Meter meter, int billingYear, int billingMonth);
    Optional<Bill> findByBillReference(String billReference);
    Page<Bill> findByBillReferenceContainingIgnoreCase(String billReference, Pageable pageable);
    List<Bill> findByStatusInAndDueDateBefore(Collection<com.wasac.utilitybilling.entity.enums.BillStatus> statuses, LocalDate dueDate);
}
