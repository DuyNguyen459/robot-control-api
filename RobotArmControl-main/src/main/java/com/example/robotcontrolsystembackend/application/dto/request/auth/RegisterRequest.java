package com.example.robotcontrolsystembackend.application.dto.request.auth;

import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    private Long factoryId;

    private UserRole role = UserRole.VIEWER;
}
