package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.UserDtos;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    UserDtos.UserResponse createStaffUser(UserDtos.CreateStaffUserRequest request);
    Page<UserDtos.UserResponse> list(int page, int size, String sortField, String sortDirection);
    UserDtos.UserResponse updateRoles(UUID id, UserDtos.UpdateUserRolesRequest request);
    UserDtos.UserResponse updateStatus(UUID id, UserDtos.UpdateUserStatusRequest request);
    void delete(UUID id);
}
