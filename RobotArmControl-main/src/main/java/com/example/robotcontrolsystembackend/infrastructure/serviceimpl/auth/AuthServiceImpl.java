package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.auth;

import com.example.robotcontrolsystembackend.application.dto.request.auth.LoginRequest;
import com.example.robotcontrolsystembackend.application.dto.request.auth.RegisterRequest;
import com.example.robotcontrolsystembackend.application.dto.response.auth.AuthResponse;
import com.example.robotcontrolsystembackend.application.service.auth.AuthService;
import com.example.robotcontrolsystembackend.common.exception.AppException;
import com.example.robotcontrolsystembackend.common.exception.ErrorCode;
import com.example.robotcontrolsystembackend.config.security.JwtProvider;
import com.example.robotcontrolsystembackend.domain.enumtype.UserRole;
import com.example.robotcontrolsystembackend.domain.model.User;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
            .factoryId(user.getFactory() != null ? user.getFactory().getFactoryId() : null)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Public registration: only creates VIEWER and cannot assign factory.
        if (request.getFactoryId() != null) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only ADMIN can assign factory during user creation");
        }
        if (request.getRole() != null && request.getRole() != UserRole.VIEWER) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only ADMIN can set role during registration");
        }

        // Check existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED, "Username already exists");
        }

        // Check existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED, "Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.VIEWER)
                .build();

        user = userRepository.save(user);

        String token = jwtProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
            .factoryId(user.getFactory() != null ? user.getFactory().getFactoryId() : null)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
