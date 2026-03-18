package com.example.robotcontrolsystembackend.application.dto.response.auth;

import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private Long factoryId;
    private String username;
    private String email;
    private UserRole role;
}
