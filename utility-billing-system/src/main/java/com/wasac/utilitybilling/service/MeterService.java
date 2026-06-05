package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MeterService {
    MeterDtos.MeterResponse create(MeterDtos.MeterRequest request);
    MeterDtos.MeterResponse update(UUID id, MeterDtos.MeterRequest request);
    MeterDtos.MeterResponse get(UUID id);
    Page<MeterDtos.MeterResponse> search(String query, int page, int size, String sortField, String sortDirection);
    void delete(UUID id);
}
