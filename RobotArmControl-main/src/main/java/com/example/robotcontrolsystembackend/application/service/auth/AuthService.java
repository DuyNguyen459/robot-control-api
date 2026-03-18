package com.example.robotcontrolsystembackend.application.service.auth;

import com.example.robotcontrolsystembackend.application.dto.request.auth.LoginRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.RegisterRequest;
import com.example.robotcontrolsystembackend.application.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
