package com.example.robotcontrolsystembackend.presentation.controller.auth;

import com.example.robotcontrolsystembackend.application.dto.request.auth.CreateUserRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.UpdateUserRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.UpdateUserRoleRequest;
import com.example.robotcontrolsystembackend.application.dto.response.auth.UserResponse;
import com.example.robotcontrolsystembackend.application.service.auth.UserService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.common.response.PageResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import com.example.robotcontrolsystembackend.domain.enumtype.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User Management Controller - ADMIN only
 * Allows ADMIN users to manage all user accounts including:
 * - Create new users with role assignment (ADMIN, OPERATOR, VIEWER)
 * - Update user information and roles
 * - Activate/Deactivate users
 * - Delete users
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user account management (ADMIN only)")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user with specified role (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination and filtering (ADMIN only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<UserResponse> response = userService.getAllUsers(pageable, role, status, search);
        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Users retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Change user role (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserResponse response = userService.updateUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User role updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Activate or deactivate a user account. Body: {\"status\":\"ACTIVE\"} or {\"status\":\"INACTIVE\"} (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "");
        if ("ACTIVE".equalsIgnoreCase(status)) {
            UserResponse response = userService.activateUser(userId);
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User activated successfully")
                    .data(response)
                    .build());
        } else if ("INACTIVE".equalsIgnoreCase(status)) {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User deactivated successfully")
                    .build());
        }
        return ResponseEntity.badRequest().body(ApiResponse.<UserResponse>builder()
                .success(false)
                .message("Invalid status. Allowed values: ACTIVE, INACTIVE")
                .build());
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Permanently delete a user account (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User deleted successfully")
                .build());
    }
}
