package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.CustomerDtos;
import com.wasac.utilitybilling.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR: create customer")
    ApiResponse<CustomerDtos.CustomerResponse> create(@Valid @RequestBody CustomerDtos.CustomerRequest request) {
        return ApiResponse.ok("Customer created", service.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER: get customer")
    ApiResponse<CustomerDtos.CustomerResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Customer retrieved", service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: search customers")
    ApiResponse<Page<CustomerDtos.CustomerResponse>> search(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortField,
                                                            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Customers retrieved", service.search(query, page, size, sortField, sortDirection));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR: update customer")
    ApiResponse<CustomerDtos.CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerDtos.CustomerRequest request) {
        return ApiResponse.ok("Customer updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete customer")
    ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Customer deleted", null);
    }
}
