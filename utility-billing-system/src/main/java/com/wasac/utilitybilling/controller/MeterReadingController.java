package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.MeterReadingDtos;
import com.wasac.utilitybilling.service.MeterReadingService;
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
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {
    private final MeterReadingService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR: capture meter reading")
    ApiResponse<MeterReadingDtos.MeterReadingResponse> create(@Valid @RequestBody MeterReadingDtos.MeterReadingRequest request) {
        return ApiResponse.ok("Meter reading captured", service.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: get meter reading")
    ApiResponse<MeterReadingDtos.MeterReadingResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Meter reading retrieved", service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE: list meter readings")
    ApiResponse<Page<MeterReadingDtos.MeterReadingResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Meter readings retrieved", service.list(page, size, sortField, sortDirection));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR: update meter reading")
    ApiResponse<MeterReadingDtos.MeterReadingResponse> update(@PathVariable UUID id, @Valid @RequestBody MeterReadingDtos.MeterReadingRequest request) {
        return ApiResponse.ok("Meter reading updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete meter reading")
    ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Meter reading deleted", null);
    }
}
