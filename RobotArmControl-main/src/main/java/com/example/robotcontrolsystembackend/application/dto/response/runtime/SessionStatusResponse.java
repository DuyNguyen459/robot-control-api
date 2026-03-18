package com.example.robotcontrolsystembackend.application.dto.response.runtime;

import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for control session status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusResponse {
    
    /**
     * Whether a session is currently active
     */
    private boolean sessionActive;
    
    /**
     * Current control mode (null if no active session)
     */
    private ControlMode controlMode;
    
    /**
     * Number of connected WebSocket clients
     */
    private int connectedClients;
    
    /**
     * Whether AI Camera is currently active
     */
    private boolean cameraActive;
    
    /**
     * Session message/description
     */
    private String message;

    /**
     * Active/current session id (null if no active session)
     */
    private Long sessionId;

    /**
     * Session owner user id
     */
    private Long userId;

    /**
     * Factory id snapshot associated with the session
     */
    private Long factoryId;

    /**
     * Controlled device id for this session
     */
    private Long deviceId;

    /**
     * Session persistence status in PostgreSQL
     */
    private SessionStatus sessionStatus;

    /**
     * Session timestamps
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
