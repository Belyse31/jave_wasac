package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.BillingDtos;
import com.wasac.utilitybilling.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "ROLE_FINANCE: record partial or full bill payment")
    ApiResponse<BillingDtos.PaymentResponse> record(@Valid @RequestBody BillingDtos.PaymentRequest request) {
        BillingDtos.PaymentResponse payment = service.record(request);
        return ApiResponse.ok("Payment recorded. Remaining balance: " + payment.billOutstandingBalance().toPlainString() + " FRW", payment);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_FINANCE, ROLE_CUSTOMER: search payment history")
    ApiResponse<Page<BillingDtos.PaymentResponse>> search(@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortField,
                                                          @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Payments retrieved", service.search(query, page, size, sortField, sortDirection));
    }
}
