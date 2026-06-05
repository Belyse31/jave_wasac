package com.wasac.utilitybilling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class MeterReadingDtos {
    private MeterReadingDtos() {
    }

    public record MeterReadingRequest(
            @Schema(example = "22222222-2222-2222-2222-222222222222")
            @NotNull UUID meterId,
            @Schema(example = "500")
            @NotNull @DecimalMin(value = "0.0") BigDecimal previousReading,
            @Schema(example = "650")
            @NotNull @DecimalMin(value = "0.0") BigDecimal currentReading,
            @Schema(example = "2026-05-31")
            @NotNull @PastOrPresent LocalDate readingDate
    ) {
    }

    public record MeterReadingResponse(UUID id, UUID meterId, String meterNumber, BigDecimal previousReading, BigDecimal currentReading, BigDecimal consumption, LocalDate readingDate, int billingYear, int billingMonth) {
    }
}
