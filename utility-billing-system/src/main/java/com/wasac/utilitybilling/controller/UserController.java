package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.UserDtos;
import com.wasac.utilitybilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: create operator, finance, or admin user with temporary password")
    ApiResponse<UserDtos.UserResponse> create(@Valid @RequestBody UserDtos.CreateStaffUserRequest request) {
        return ApiResponse.ok("Staff user created with temporary password", userService.createStaffUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: list users with pagination and sorting")
    ApiResponse<Page<UserDtos.UserResponse>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.ok("Users retrieved", userService.list(page, size, sortField, sortDirection));
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: update user roles")
    ApiResponse<UserDtos.UserResponse> roles(@PathVariable UUID id, @Valid @RequestBody UserDtos.UpdateUserRolesRequest request) {
        return ApiResponse.ok("User roles updated", userService.updateRoles(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: update user status")
    ApiResponse<UserDtos.UserResponse> status(@PathVariable UUID id, @Valid @RequestBody UserDtos.UpdateUserStatusRequest request) {
        return ApiResponse.ok("User status updated", userService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ROLE_ADMIN: delete user")
    ApiResponse<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ApiResponse.ok("User deleted", null);
    }
}
