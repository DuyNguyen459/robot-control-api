package com.example.robotcontrolsystembackend.presentation.controller;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.AiAnglesRequest;
import com.example.robotcontrolsystembackend.application.dto.request.runtime.StartSessionRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.SessionStatusResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.ControlSessionService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.config.websocket.RobotControlWebSocketHandler;
import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
@Tag(name = "AI Camera", description = "APIs for starting/stopping AI Camera control mode via WebSocket broadcast (edge devices run the camera)")
public class CameraController {

    private final ControlSessionService controlSessionService;
    private final RobotControlWebSocketHandler webSocketHandler;

    @PostMapping("/start")
    @Operation(summary = "Start AI camera", description = "Start CAMERA control session and broadcast START over WebSocket. Edge devices that run ai_camera.py will activate their webcam. ADMIN and OPERATOR only.")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> startCamera() {
        try {
        SessionStatusResponse response = controlSessionService.startSession(
            StartSessionRequest.builder().controlMode(ControlMode.CAMERA).build()
        );

            String message = response.getMessage();
            if (response.getConnectedClients() == 0) {
                message = message + " | WARNING: No WebSocket clients connected. START was broadcast, but no edge camera will activate until a client connects to /ws/robot-control.";
            }

            return ResponseEntity.ok(ApiResponse.ok(message, response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail("CAMERA_START_FAILED", "Failed to start AI Camera: " + e.getMessage())
            );
        }
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop AI camera", description = "Stop current control session and broadcast STOP over WebSocket. Edge devices will stop their camera. ADMIN and OPERATOR only.")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> stopCamera() {
        try {
        SessionStatusResponse response = controlSessionService.stopSession();
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail("CAMERA_STOP_FAILED", "Failed to stop AI Camera: " + e.getMessage())
            );
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get AI camera status", description = "Get current control session status (CAMERA/BUTTON) and number of connected WebSocket clients. Note: server cannot directly know webcam availability on edge devices.")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> getCameraStatus() {
        try {
        SessionStatusResponse response = controlSessionService.getSessionStatus();
        return ResponseEntity.ok(ApiResponse.ok("Camera status retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail("CAMERA_STATUS_FAILED", "Failed to get AI Camera status: " + e.getMessage())
            );
        }
    }

    @PostMapping("/angles")
    @Operation(
            summary = "Send AI angles (REST fallback)",
            description = "Fallback endpoint: browser can POST ai_angles and server will broadcast to /ws/robot-control for Unity clients. ADMIN and OPERATOR only."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> sendAiAngles(@Valid @org.springframework.web.bind.annotation.RequestBody AiAnglesRequest request) {
        try {
            List<Double> angles = request.getAngles();
            if (angles == null || angles.size() != 6) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("AI_ANGLES_INVALID", "angles must have exactly 6 values"));
            }

            // Validate numeric inputs
            for (Double a : angles) {
                if (a == null || a.isNaN() || a.isInfinite()) {
                    return ResponseEntity.badRequest().body(ApiResponse.fail("AI_ANGLES_INVALID", "angles values must be finite numbers"));
                }
            }

            // Broadcast as the same payload Unity expects.
            String payload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(
                    Map.of("type", "ai_angles", "angles", angles)
            );
            webSocketHandler.broadcastMessage(payload);

            return ResponseEntity.ok(ApiResponse.ok("ai_angles broadcast", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.fail("AI_ANGLES_SEND_FAILED", "Failed to send ai_angles: " + e.getMessage())
            );
        }
    }
}