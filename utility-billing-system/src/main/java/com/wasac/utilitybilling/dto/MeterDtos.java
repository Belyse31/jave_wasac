package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.MeterStatus;
import com.wasac.utilitybilling.entity.enums.MeterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public final class MeterDtos {
    private MeterDtos() {
    }

    public record MeterRequest(
            @Schema(example = "WM-10001")
            @NotBlank @Pattern(regexp = ValidationPatterns.METER_NUMBER, message = "Meter number must contain uppercase letters, numbers, or hyphens") String meterNumber,
            @Schema(example = "WATER")
            @NotNull MeterType meterType,
            @Schema(example = "2026-01-15")
            @NotNull @PastOrPresent LocalDate installationDate,
            @Schema(example = "ACTIVE")
            @NotNull MeterStatus status,
            @Schema(example = "11111111-1111-1111-1111-111111111111")
            @NotNull UUID customerId
    ) {
    }

    public record MeterResponse(UUID id, String meterNumber, MeterType meterType, LocalDate installationDate, MeterStatus status, UUID customerId, String customerName) {
    }
}
