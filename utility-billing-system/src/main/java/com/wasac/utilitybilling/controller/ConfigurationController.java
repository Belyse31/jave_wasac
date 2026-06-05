package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.ConfigDtos;
import com.wasac.utilitybilling.service.ConfigurationService;
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
@RequestMapping("/api/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationController {
    private final ConfigurationService service;

    @PostMapping("/tariffs")
    @Operation(summary = "ROLE_ADMIN: configure versioned tariff")
    ApiResponse<ConfigDtos.TariffResponse> createTariff(@Valid @RequestBody ConfigDtos.TariffRequest request) {
        return ApiResponse.ok("Tariff created", service.createTariff(request));
    }

    @GetMapping("/tariffs")
    @Operation(summary = "ROLE_ADMIN: list tariffs")
    ApiResponse<Page<ConfigDtos.TariffResponse>> tariffs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                         @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Tariffs retrieved", service.tariffs(page, size, sortField, sortDirection));
    }

    @PutMapping("/tariffs/{id}")
    @Operation(summary = "ROLE_ADMIN: update tariff")
    ApiResponse<ConfigDtos.TariffResponse> updateTariff(@PathVariable UUID id, @Valid @RequestBody ConfigDtos.TariffRequest request) {
        return ApiResponse.ok("Tariff updated", service.updateTariff(id, request));
    }

    @DeleteMapping("/tariffs/{id}")
    @Operation(summary = "ROLE_ADMIN: delete tariff")
    ApiResponse<Void> deleteTariff(@PathVariable UUID id) {
        service.deleteTariff(id);
        return ApiResponse.ok("Tariff deleted", null);
    }

    @PostMapping("/taxes")
    @Operation(summary = "ROLE_ADMIN: configure versioned tax")
    ApiResponse<ConfigDtos.TaxResponse> createTax(@Valid @RequestBody ConfigDtos.TaxRequest request) {
        return ApiResponse.ok("Tax created", service.createTax(request));
    }

    @GetMapping("/taxes")
    @Operation(summary = "ROLE_ADMIN: list taxes")
    ApiResponse<Page<ConfigDtos.TaxResponse>> taxes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Taxes retrieved", service.taxes(page, size, sortField, sortDirection));
    }

    @PutMapping("/taxes/{id}")
    @Operation(summary = "ROLE_ADMIN: update tax")
    ApiResponse<ConfigDtos.TaxResponse> updateTax(@PathVariable UUID id, @Valid @RequestBody ConfigDtos.TaxRequest request) {
        return ApiResponse.ok("Tax updated", service.updateTax(id, request));
    }

    @DeleteMapping("/taxes/{id}")
    @Operation(summary = "ROLE_ADMIN: delete tax")
    ApiResponse<Void> deleteTax(@PathVariable UUID id) {
        service.deleteTax(id);
        return ApiResponse.ok("Tax deleted", null);
    }

    @PostMapping("/penalties")
    @Operation(summary = "ROLE_ADMIN: configure versioned late payment penalty")
    ApiResponse<ConfigDtos.PenaltyResponse> createPenalty(@Valid @RequestBody ConfigDtos.PenaltyRequest request) {
        return ApiResponse.ok("Penalty created", service.createPenalty(request));
    }

    @GetMapping("/penalties")
    @Operation(summary = "ROLE_ADMIN: list penalties")
    ApiResponse<Page<ConfigDtos.PenaltyResponse>> penalties(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Penalties retrieved", service.penalties(page, size, sortField, sortDirection));
    }

    @PutMapping("/penalties/{id}")
    @Operation(summary = "ROLE_ADMIN: update penalty")
    ApiResponse<ConfigDtos.PenaltyResponse> updatePenalty(@PathVariable UUID id, @Valid @RequestBody ConfigDtos.PenaltyRequest request) {
        return ApiResponse.ok("Penalty updated", service.updatePenalty(id, request));
    }

    @DeleteMapping("/penalties/{id}")
    @Operation(summary = "ROLE_ADMIN: delete penalty")
    ApiResponse<Void> deletePenalty(@PathVariable UUID id) {
        service.deletePenalty(id);
        return ApiResponse.ok("Penalty deleted", null);
    }
}
