package com.example.robotcontrolsystembackend.application.dto.request.auth;

import com.example.robotcontrolsystembackend.domain.enumtype.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private UserStatus status;

    private Long factoryId;
}
