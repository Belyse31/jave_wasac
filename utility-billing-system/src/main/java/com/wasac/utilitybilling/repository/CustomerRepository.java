package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Page<Customer> findByFullNameContainingIgnoreCaseOrNationalIdContainingIgnoreCase(String name, String nationalId, Pageable pageable);
}
