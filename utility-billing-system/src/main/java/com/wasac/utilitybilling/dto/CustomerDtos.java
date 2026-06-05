package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public final class CustomerDtos {
    private CustomerDtos() {
    }

    public record CustomerRequest(
            @Schema(example = "Jean Mutabazi")
            @NotBlank String fullName,
            @Schema(example = "1199988776655443")
            @NotBlank @Pattern(regexp = ValidationPatterns.NATIONAL_ID_OR_PASSPORT, message = "National ID must be 16 digits or a valid passport format") String nationalId,
            @Schema(example = "jean.mutabazi@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be lowercase") String email,
            @Schema(example = "0781234567")
            @NotBlank @Pattern(regexp = ValidationPatterns.RWANDA_PHONE, message = "Phone number must be a valid Rwanda mobile number") String phoneNumber,
            @Schema(example = "Kigali, Rwanda")
            @NotBlank String address,
            @Schema(example = "1995-05-10")
            @PastOrPresent(message = "Date of birth cannot be a future date")
            LocalDate dateOfBirth,
            @Schema(example = "ACTIVE")
            @NotNull CustomerStatus status
    ) {
    }

    public record CustomerResponse(UUID id, String fullName, String nationalId, String email, String phoneNumber, String address, LocalDate dateOfBirth, CustomerStatus status) {
    }
}
