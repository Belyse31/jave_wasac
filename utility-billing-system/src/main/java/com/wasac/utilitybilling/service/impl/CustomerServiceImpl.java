package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.CustomerDtos;
import com.wasac.utilitybilling.entity.Customer;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.service.CustomerService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;

    @Override
    @Transactional
    public CustomerDtos.CustomerResponse create(CustomerDtos.CustomerRequest request) {
        // Check National ID/passport, email, and phone before creating the customer.
        checkDuplicates(request, null);
        // Create a fresh customer entity only after duplicate validation passes.
        Customer customer = new Customer();
        // Copy validated request data into the entity.
        apply(customer, request);
        // Save and return DTO so the API never exposes the entity directly.
        return toResponse(repository.save(customer));
    }

    @Override
    @Transactional
    public CustomerDtos.CustomerResponse update(UUID id, CustomerDtos.CustomerRequest request) {
        // Load the current customer or return a 404 error.
        Customer customer = entity(id);
        // Duplicate checks ignore the current record's own values.
        checkDuplicates(request, customer);
        // Apply validated update values to the managed entity.
        apply(customer, request);
        // Transaction commits the changes automatically.
        return toResponse(customer);
    }

    @Override
    public CustomerDtos.CustomerResponse get(UUID id) {
        return toResponse(entity(id));
    }

    @Override
    public Page<CustomerDtos.CustomerResponse> search(String query, int page, int size, String sortField, String sortDirection) {
        var pageable = PageUtils.pageable(page, size, sortField, sortDirection);
        String q = query == null ? "" : query;
        return repository.findByFullNameContainingIgnoreCaseOrNationalIdContainingIgnoreCase(q, q, pageable).map(this::toResponse);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(entity(id));
    }

    private Customer entity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private void checkDuplicates(CustomerDtos.CustomerRequest request, Customer current) {
        // National ID/passport must be unique because it identifies one real customer.
        if ((current == null || !current.getNationalId().equals(request.nationalId())) && repository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException("National ID is already registered");
        }
        // Customer email must be unique for billing and notification delivery.
        if ((current == null || !current.getEmail().equals(request.email())) && repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Customer email is already registered");
        }
        // Customer phone must be unique for contact and recovery workflows.
        if ((current == null || !current.getPhoneNumber().equals(request.phoneNumber())) && repository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Customer phone number is already registered");
        }
    }

    private void apply(Customer customer, CustomerDtos.CustomerRequest request) {
        // DOB is optional, but when provided it cannot be in the future.
        if (request.dateOfBirth() != null && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Date of birth cannot be a future date");
        }
        // Customer must be older than 16 years, so exactly 16 is still rejected.
        if (request.dateOfBirth() != null && Period.between(request.dateOfBirth(), LocalDate.now()).getYears() <= 16) {
            throw new BusinessRuleException("Customer must be greater than 16 years old");
        }
        // Store the customer's legal or preferred full name.
        customer.setFullName(request.fullName());
        // Store either a 16-digit national ID or a passport number.
        customer.setNationalId(request.nationalId());
        // Store validated lowercase email.
        customer.setEmail(request.email());
        // Store validated Rwanda phone number.
        customer.setPhoneNumber(request.phoneNumber());
        // Store physical address used for service location context.
        customer.setAddress(request.address());
        // Store DOB for age validation and reporting.
        customer.setDateOfBirth(request.dateOfBirth());
        // Store ACTIVE, INACTIVE, or SUSPENDED customer status.
        customer.setStatus(request.status());
    }

    private CustomerDtos.CustomerResponse toResponse(Customer c) {
        return new CustomerDtos.CustomerResponse(c.getId(), c.getFullName(), c.getNationalId(), c.getEmail(), c.getPhoneNumber(), c.getAddress(), c.getDateOfBirth(), c.getStatus());
    }
}
