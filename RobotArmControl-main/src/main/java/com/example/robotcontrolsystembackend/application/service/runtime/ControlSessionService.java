package com.example.robotcontrolsystembackend.application.service.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.StartSessionRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.SessionStatusResponse;
import com.example.robotcontrolsystembackend.common.response.PageResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;

public interface ControlSessionService {
    
    /**
     * Start a control session with specified control mode
     * @param request contains control mode (CAMERA or BUTTON) and optional device ID
     * @return session status response
     */
    SessionStatusResponse startSession(StartSessionRequest request);
    
    /**
     * Stop the current control session
     * @return session status response
     */
    SessionStatusResponse stopSession();
    
    /**
     * Get current session status
     * @return session status response
     */
    SessionStatusResponse getSessionStatus();

    /**
     * List control sessions from PostgreSQL
     */
    PageResponse<SessionStatusResponse> getSessions(SessionStatus status, int page, int size);

    /**
     * Get one session by id from PostgreSQL
     */
    SessionStatusResponse getSessionById(Long sessionId);
    
    /**
     * Check if a session is currently active
     * @return true if session is active
     */
    boolean isSessionActive();
    
    /**
     * Get current control mode
     * @return current control mode or null if no active session
     */
    ControlMode getCurrentControlMode();
    
    /**
     * Get the number of connected WebSocket clients
     * @return number of connected clients
     */
    int getConnectedClientsCount();
    
    /**
     * Send robot angle command (for BUTTON mode)
     * @param jointIndex joint index (0-5)
     * @param angle target angle
     * @return true if sent successfully
     */
    boolean sendAngleCommand(int jointIndex, double angle);
    
    /**
     * Send all robot angles (for BUTTON mode)
     * @param angles array of 6 angles
     * @return true if sent successfully
     */
    boolean sendAllAngles(double[] angles);
}
