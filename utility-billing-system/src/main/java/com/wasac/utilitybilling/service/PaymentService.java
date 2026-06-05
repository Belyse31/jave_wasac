package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.BillingDtos;
import org.springframework.data.domain.Page;

public interface PaymentService {
    BillingDtos.PaymentResponse record(BillingDtos.PaymentRequest request);
    Page<BillingDtos.PaymentResponse> search(String query, int page, int size, String sortField, String sortDirection);
}
