package com.example.robotcontrolsystembackend.config.websocket;

import com.example.robotcontrolsystembackend.config.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        // Nếu có token, validate và lưu username
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtProvider.validateToken(token)) {
                    String username = jwtProvider.getUsernameFromToken(token);
                    attributes.put("username", username);
                    attributes.put("authenticated", true);
                    log.info("WebSocket connection authorized for user: {}", username);
                    return true;
                }
            } catch (Exception e) {
                log.warn("WebSocket JWT validation failed: {}", e.getMessage());
            }
        }
        
        // Cho phép kết nối không cần token (Unity robot)
        attributes.put("username", "anonymous");
        attributes.put("authenticated", false);
        log.info("WebSocket connection allowed without authentication (Unity client)");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed
    }
}
