package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.BillingDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BillService {
    BillingDtos.BillResponse generate(BillingDtos.BillGenerateRequest request);
    int generateMonthly(BillingDtos.MonthlyBillingRequest request);
    BillingDtos.BillResponse approve(UUID id);
    BillingDtos.BillResponse get(UUID id);
    Page<BillingDtos.BillResponse> search(String query, int page, int size, String sortField, String sortDirection);
}
