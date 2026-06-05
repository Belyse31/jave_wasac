package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.AccountStatus;
import com.wasac.utilitybilling.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(UUID id, String fullName, String email, String phoneNumber, AccountStatus status, boolean emailVerified, boolean passwordChangeRequired, Set<Role> roles) {
    }

    public record CreateStaffUserRequest(
            @Schema(example = "Alice")
            @NotBlank(message = "First name is required") String firstName,
            @Schema(example = "Mukamana")
            @NotBlank(message = "Last name is required") String lastName,
            @Schema(example = "operator@gmail.com")
            @NotBlank @Email @Pattern(regexp = ValidationPatterns.LOWERCASE_EMAIL, message = "Email must be valid and lowercase") String email,
            @Schema(example = "0781234567")
            @NotBlank @Pattern(regexp = ValidationPatterns.RWANDA_PHONE, message = "Phone number must be a valid Rwanda mobile number") String phoneNumber,
            @Schema(example = "ROLE_OPERATOR")
            @NotNull Role role
    ) {
    }

    public record UpdateUserRolesRequest(@Schema(example = "[\"ROLE_OPERATOR\"]") @NotEmpty Set<Role> roles) {
    }

    public record UpdateUserStatusRequest(@Schema(example = "ACTIVE") AccountStatus status) {
    }
}
