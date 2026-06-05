package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.MeterType;
import com.wasac.utilitybilling.entity.enums.PenaltyType;
import com.wasac.utilitybilling.entity.enums.TariffModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ConfigDtos {
    private ConfigDtos() {
    }

    public record TariffTierRequest(@Schema(example = "0") @NotNull @DecimalMin("0.0") BigDecimal fromUnits,
                                    @Schema(example = "10") BigDecimal toUnits,
                                    @Schema(example = "350") @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal rate) {
    }

    public record TariffRequest(@Schema(example = "Water Residential Tariff") @NotBlank String name,
                                @Schema(example = "WATER") @NotNull MeterType meterType,
                                @Schema(example = "FLAT") @NotNull TariffModel model,
                                @Schema(example = "500") @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal rate,
                                @Schema(example = "1000") @NotNull @DecimalMin("0.0") BigDecimal fixedCharge,
                                @Schema(example = "2026-07-01") @NotNull @FutureOrPresent LocalDate effectiveFrom,
                                @Schema(example = "2026-12-31") LocalDate effectiveTo,
                                @Valid List<TariffTierRequest> tiers) {
    }

    public record TariffResponse(UUID id, String name, MeterType meterType, TariffModel model, BigDecimal rate, BigDecimal fixedCharge,
                                 LocalDate effectiveFrom, LocalDate effectiveTo, int version, boolean active, List<TariffTierRequest> tiers) {
    }

    public record TaxRequest(@Schema(example = "VAT") @NotBlank String name,
                             @Schema(example = "0.18") @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal rate,
                             @Schema(example = "2026-07-01") @NotNull @FutureOrPresent LocalDate effectiveFrom,
                             @Schema(example = "2026-12-31") LocalDate effectiveTo) {
    }

    public record TaxResponse(UUID id, String name, BigDecimal rate, LocalDate effectiveFrom, LocalDate effectiveTo, int version, boolean active) {
    }

    public record PenaltyRequest(@Schema(example = "Late Payment Penalty") @NotBlank String name,
                                 @Schema(example = "PERCENTAGE") @NotNull PenaltyType type,
                                 @Schema(example = "0.05") @NotNull @DecimalMin("0.0") BigDecimal value,
                                 @Schema(example = "2026-07-01") @NotNull @FutureOrPresent LocalDate effectiveFrom,
                                 @Schema(example = "2026-12-31") LocalDate effectiveTo) {
    }

    public record PenaltyResponse(UUID id, String name, PenaltyType type, BigDecimal value, LocalDate effectiveFrom, LocalDate effectiveTo, int version, boolean active) {
    }
}
