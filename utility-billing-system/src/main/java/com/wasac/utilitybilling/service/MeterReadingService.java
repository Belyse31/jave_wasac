package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterReadingDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MeterReadingService {
    MeterReadingDtos.MeterReadingResponse create(MeterReadingDtos.MeterReadingRequest request);
    MeterReadingDtos.MeterReadingResponse update(UUID id, MeterReadingDtos.MeterReadingRequest request);
    MeterReadingDtos.MeterReadingResponse get(UUID id);
    Page<MeterReadingDtos.MeterReadingResponse> list(int page, int size, String sortField, String sortDirection);
    void delete(UUID id);
}
