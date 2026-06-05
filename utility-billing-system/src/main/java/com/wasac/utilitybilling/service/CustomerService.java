package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.CustomerDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CustomerService {
    CustomerDtos.CustomerResponse create(CustomerDtos.CustomerRequest request);
    CustomerDtos.CustomerResponse update(UUID id, CustomerDtos.CustomerRequest request);
    CustomerDtos.CustomerResponse get(UUID id);
    Page<CustomerDtos.CustomerResponse> search(String query, int page, int size, String sortField, String sortDirection);
    void delete(UUID id);
}
