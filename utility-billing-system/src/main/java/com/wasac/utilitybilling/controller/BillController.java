package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.BillingDtos;
import com.wasac.utilitybilling.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {
    private final BillService service;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: generate bill from a meter reading")
    ApiResponse<BillingDtos.BillResponse> generate(@Valid @RequestBody BillingDtos.BillGenerateRequest request) {
        return ApiResponse.ok("Bill generated", service.generate(request));
    }

    @PostMapping("/generate-monthly")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE: generate monthly bills for existing readings")
    ApiResponse<Integer> generateMonthly(@Valid @RequestBody BillingDtos.MonthlyBillingRequest request) {
        return ApiResponse.ok("Monthly billing completed", service.generateMonthly(request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "ROLE_FINANCE: approve bill")
    ApiResponse<BillingDtos.BillResponse> approve(@PathVariable UUID id) {
        return ApiResponse.ok("Bill approved", service.approve(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: get bill")
    ApiResponse<BillingDtos.BillResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Bill retrieved", service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: search bills by bill reference")
    ApiResponse<Page<BillingDtos.BillResponse>> search(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortField,
                                                       @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Bills retrieved", service.search(query, page, size, sortField, sortDirection));
    }
}
