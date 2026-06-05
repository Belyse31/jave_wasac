package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByPaymentReferenceContainingIgnoreCaseOrBill_BillReferenceContainingIgnoreCase(String paymentReference, String billReference, Pageable pageable);
}
