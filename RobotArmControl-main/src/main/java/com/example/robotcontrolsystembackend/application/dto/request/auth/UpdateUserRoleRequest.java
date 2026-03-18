package com.example.robotcontrolsystembackend.application.dto.request.auth;

import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private UserRole role;
}
