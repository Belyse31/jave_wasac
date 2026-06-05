package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.UserDtos;
import com.wasac.utilitybilling.entity.User;
import com.wasac.utilitybilling.entity.enums.AccountStatus;
import com.wasac.utilitybilling.entity.enums.Role;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.DuplicateResourceException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.service.AuditService;
import com.wasac.utilitybilling.service.EmailService;
import com.wasac.utilitybilling.service.UserService;
import com.wasac.utilitybilling.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    // Encoder hashes generated temporary passwords before storage.
    private final PasswordEncoder passwordEncoder;
    // Audit service records important administrative actions for traceability.
    private final AuditService auditService;
    // Email service sends staff credentials to the created staff user.
    private final EmailService emailService;
    // Secure random is used to generate temporary passwords.
    private final SecureRandom random = new SecureRandom();
    @Value("${app.bootstrap.admin-email}")
    private String bootstrapAdminEmail;

    @Override
    @Transactional
    public UserDtos.UserResponse createStaffUser(UserDtos.CreateStaffUserRequest request) {
        // Customers must self-register so OTP/email verification is always applied.
        if (request.role() == Role.ROLE_CUSTOMER) {
            throw new BusinessRuleException("Customers must use public registration; admin can only create staff users");
        }
        // Email uniqueness prevents two users from sharing one login identity.
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        // Phone uniqueness supports account recovery and prevents duplicate staff records.
        if (repository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("Phone number is already registered");
        }
        // Example.com is a documentation domain and cannot be used to receive staff credentials.
        if (request.email().endsWith("@example.com")) {
            throw new BusinessRuleException("Use a real email address for staff credentials. Example.com emails cannot receive messages.");
        }

        // Staff accounts are seeded by an admin with a temporary password that must be changed on first login.
        User user = new User();
        // Generate the temporary password once so the same value is stored and emailed.
        String temporaryPassword = temporaryPassword();
        // Combine first and last name into the stored full name.
        user.setFullName(request.firstName() + " " + request.lastName());
        // Store the validated lowercase email.
        user.setEmail(request.email());
        // Store the validated Rwanda phone number.
        user.setPhoneNumber(request.phoneNumber());
        // Hash the generated temporary password before saving.
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        // Force first-login password change for admin-created users.
        user.setPasswordChangeRequired(true);
        // Expire the temporary password after 24 hours.
        user.setPasswordExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        // Staff users are created by admin, so email is trusted as verified.
        user.setEmailVerified(true);
        // Staff users can login immediately unless admin disables them later.
        user.setStatus(AccountStatus.ACTIVE);
        // Assign the selected role after validating it is not customer.
        user.getRoles().add(request.role());
        // Save the staff user in the database.
        User saved = repository.save(user);
        // Email the created staff user their login credentials.
        emailService.sendStaffCredentialsEmail(saved.getEmail(), saved.getFullName(), request.role().name(), temporaryPassword);
        // Record the admin action for audit trail requirements.
        auditService.record("admin", "ADMIN_CREATED_USER", null, "Created staff user " + saved.getEmail() + " with role " + request.role());
        // Return a DTO, never the entity or password.
        return response(saved);
    }

    @Override
    public Page<UserDtos.UserResponse> list(int page, int size, String sortField, String sortDirection) {
        // Use a common paging helper so all list endpoints behave consistently.
        return repository.findAll(PageUtils.pageable(page, size, sortField, sortDirection)).map(this::response);
    }

    @Override
    @Transactional
    public UserDtos.UserResponse updateRoles(UUID id, UserDtos.UpdateUserRolesRequest request) {
        // Load the target user or fail with a friendly 404 error.
        User user = entity(id);
        // Replace roles with the validated role set from the request.
        user.setRoles(request.roles());
        // Return the updated user as a response DTO.
        return response(user);
    }

    @Override
    @Transactional
    public UserDtos.UserResponse updateStatus(UUID id, UserDtos.UpdateUserStatusRequest request) {
        // Load the target user before changing status.
        User user = entity(id);
        // ACTIVE, INACTIVE, LOCKED, and pending states are controlled through this field.
        user.setStatus(request.status());
        // Return the updated user without exposing sensitive fields.
        return response(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        // Load the user so we can validate before deleting.
        User user = entity(id);
        // Protect the configured seeded admin account from accidental deletion.
        if (user.getEmail().equalsIgnoreCase(bootstrapAdminEmail)) {
            throw new BusinessRuleException("Default admin account cannot be deleted");
        }
        // Delete roles and the user through JPA cascading/collection cleanup.
        repository.delete(user);
        // Audit user deletion for admin traceability.
        auditService.record("admin", "ADMIN_DELETED_USER", null, "Deleted user " + user.getEmail());
    }

    private User entity(UUID id) {
        // Centralized lookup keeps all user endpoints consistent.
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserDtos.UserResponse response(User u) {
        // DTO protects the password hash and exposes only API-safe user details.
        return new UserDtos.UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getStatus(), u.isEmailVerified(), u.isPasswordChangeRequired(), u.getRoles());
    }

    private String temporaryPassword() {
        // Generated password satisfies the configured strong-password rule.
        return "Tmp@" + (100000 + random.nextInt(900000)) + "Aa";
    }
}
