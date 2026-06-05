package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.BillStatus;
import com.wasac.utilitybilling.entity.enums.MeterType;
import com.wasac.utilitybilling.entity.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class BillingDtos {
    private BillingDtos() {
    }

    public record BillGenerateRequest(@Schema(example = "33333333-3333-3333-3333-333333333333") @NotNull UUID readingId,
                                      @Schema(example = "2026-06-30") @NotNull @FutureOrPresent LocalDate dueDate) {
    }

    public record MonthlyBillingRequest(@Schema(example = "2026") @Min(2000) int year,
                                        @Schema(example = "5") @Min(1) @Max(12) int month,
                                        @Schema(example = "2026-06-30") @NotNull LocalDate dueDate) {
    }

    public record BillResponse(UUID id, String billReference, UUID customerId, String customerName, UUID meterId, String meterNumber,
                               MeterType meterType, int billingYear, int billingMonth, BigDecimal consumption,
                               BigDecimal consumptionCharge, BigDecimal fixedCharge, BigDecimal taxAmount,
                               BigDecimal penaltyAmount, BigDecimal totalAmount, BigDecimal outstandingBalance,
                               LocalDate dueDate, BillStatus status, boolean approved) {
    }

    public record PaymentRequest(@Schema(example = "44444444-4444-4444-4444-444444444444") @NotNull UUID billId,
                                 @Schema(example = "15000") @NotNull @DecimalMin(value = "0.01") BigDecimal amountPaid,
                                 @Schema(example = "MOMO") @NotNull PaymentMethod paymentMethod,
                                 @Schema(example = "2026-06-05") @NotNull @PastOrPresent LocalDate paymentDate) {
    }

    public record PaymentResponse(UUID id, String paymentReference, String billReference, BigDecimal amountPaid, PaymentMethod paymentMethod, LocalDate paymentDate, BigDecimal billOutstandingBalance, BillStatus billStatus) {
    }
}
