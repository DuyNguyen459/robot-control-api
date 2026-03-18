package com.example.robotcontrolsystembackend.application.service.auth;

import com.example.robotcontrolsystembackend.application.dto.request.auth.CreateUserRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.UpdateUserRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.UpdateUserRoleRequest;
import com.example.robotcontrolsystembackend.application.dto.response.auth.UserResponse;
import com.example.robotcontrolsystembackend.common.response.PageResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import com.example.robotcontrolsystembackend.domain.enumtype.UserStatus;
import org.springframework.data.domain.Pageable;

/**
 * User Management Service - ADMIN only operations
 */
public interface UserService {
    
    /**
     * Create a new user (ADMIN only)
     */
    UserResponse createUser(CreateUserRequest request);
    
    /**
     * Get user by ID
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Get all users with pagination and filtering
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable, UserRole role, UserStatus status, String search);
    
    /**
     * Update user information (ADMIN only)
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    
    /**
     * Update user role (ADMIN only)
     */
    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);
    
    /**
     * Deactivate user (ADMIN only)
     */
    void deactivateUser(Long userId);
    
    /**
     * Activate user (ADMIN only)
     */
    UserResponse activateUser(Long userId);
    
    /**
     * Delete user (ADMIN only)
     */
    void deleteUser(Long userId);
}
