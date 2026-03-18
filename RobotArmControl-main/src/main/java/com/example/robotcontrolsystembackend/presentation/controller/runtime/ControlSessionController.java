package com.example.robotcontrolsystembackend.presentation.controller.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.StartSessionRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.SessionStatusResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.ControlSessionService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.common.response.PageResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Control Session Controller
 * Manages robot control sessions with CAMERA or BUTTON mode
 * - GET operations: All authenticated users (ADMIN, OPERATOR, VIEWER)
 * - POST/PATCH/DELETE operations: ADMIN and OPERATOR only
 *
 * Endpoints:
 *   POST   /api/control-sessions                    — start a new session (replaces /start)
 *   GET    /api/control-sessions                    — list sessions (?status=&page=&size=)
 *   GET    /api/control-sessions/{sessionId}        — get single session
 *   PUT    /api/control-sessions/{sessionId}        — update session
 *   PATCH  /api/control-sessions/{sessionId}/status — update session status (e.g., end it)
 *   DELETE /api/control-sessions/{sessionId}        — delete session
 *   GET    /api/control-sessions/current            — get current active session (replaces /status)
 *   PATCH  /api/control-sessions/current/status     — stop current session (replaces POST /stop)
 *   POST   /api/control-sessions/current/joint-commands      — send single joint angle (BUTTON mode)
 *   POST   /api/control-sessions/current/joint-commands/bulk — send all joint angles (BUTTON mode)
 */
@RestController
@RequestMapping("/api/control-sessions")
@RequiredArgsConstructor
@Tag(name = "Control Session", description = "APIs for managing robot control sessions")
public class ControlSessionController {

    private final ControlSessionService controlSessionService;

    // ==================== COLLECTION CRUD ====================

    // GET /api/control-sessions?status=ACTIVE&page=0&size=10
    @GetMapping
    @Operation(summary = "Get all sessions", description = "Retrieve all control sessions. Filter by status (ACTIVE/ENDED/etc.), paginate with page & size (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<SessionStatusResponse>>> getAllSessions(
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SessionStatusResponse> response = controlSessionService.getSessions(status, page, size);
        return ResponseEntity.ok(ApiResponse.<PageResponse<SessionStatusResponse>>builder()
                .success(true)
                .message("Sessions retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session by ID", description = "Retrieve a specific control session (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
        public ResponseEntity<ApiResponse<SessionStatusResponse>> getSessionById(@PathVariable Long sessionId) {
                SessionStatusResponse response = controlSessionService.getSessionById(sessionId);
                return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(true)
                .message("Session retrieved successfully")
                                .data(response)
                .build());
    }

    // POST /api/control-sessions  — replaces /start; body contains controlMode (CAMERA|BUTTON)
    @PostMapping
    @Operation(summary = "Start control session",
               description = "Start a new control session with CAMERA or BUTTON mode. " +
                           "CAMERA mode activates AI Camera for hand gesture control. " +
                           "BUTTON mode enables manual control via frontend angle commands. " +
                           "(ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> startSession(
            @Valid @RequestBody StartSessionRequest request) {
        SessionStatusResponse response = controlSessionService.startSession(request);
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(response.isSessionActive())
                .message(response.getMessage())
                .data(response)
                .build());
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Update a control session (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateSession(
            @PathVariable Long sessionId,
            @RequestBody Object request) {
        // TODO: Implement with ControlSessionService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Session updated successfully")
                .data("Updated session " + sessionId)
                .build());
    }

    // PATCH /api/control-sessions/{sessionId}/status  body: {"status":"ENDED"}
    @PatchMapping("/{sessionId}/status")
    @Operation(summary = "Update session status", description = "Update the status of a specific session (e.g., end it). Body: {\"status\":\"ENDED\"} (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateSessionStatus(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> body) {
        // TODO: Implement with ControlSessionService — route to appropriate action based on body.get("status")
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Session status updated successfully")
                .data("Session " + sessionId + " status: " + body.get("status"))
                .build());
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete session", description = "Delete a control session record (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long sessionId) {
        // TODO: Implement with ControlSessionService
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Session deleted successfully")
                .build());
    }

    // ==================== CURRENT ACTIVE SESSION ====================

    // GET /api/control-sessions/current  — replaces /status
    @GetMapping("/current")
    @Operation(summary = "Get current session",
               description = "Get current active control session status including mode and camera state (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> getCurrentSession() {
        SessionStatusResponse response = controlSessionService.getSessionStatus();
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

        @GetMapping("/current/context")
        @Operation(summary = "Get current session context for robot",
                           description = "Get current active session context (sessionId, userId, factoryId, deviceId) for robot log binding")
        @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
        public ResponseEntity<ApiResponse<SessionStatusResponse>> getCurrentSessionContext() {
                SessionStatusResponse response = controlSessionService.getSessionStatus();
                return ResponseEntity.ok(ApiResponse.ok("Session context retrieved", response));
        }

    // PATCH /api/control-sessions/current/status  — replaces POST /stop
    @PatchMapping("/current/status")
    @Operation(summary = "Stop current session",
               description = "Stop the current active control session (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> stopCurrentSession() {
        SessionStatusResponse response = controlSessionService.stopSession();
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    // ==================== BUTTON MODE JOINT COMMANDS ====================

    // POST /api/control-sessions/current/joint-commands?jointIndex=0&angle=45.0  — replaces /button/angle
    @PostMapping("/current/joint-commands")
    @Operation(summary = "Send single joint angle command",
               description = "Send angle command for a specific joint (BUTTON mode only). " +
                           "Query params: jointIndex (0-5), angle (degrees). (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> sendJointCommand(
            @RequestParam int jointIndex,
            @RequestParam double angle) {
        if (controlSessionService.getCurrentControlMode() != ControlMode.BUTTON) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Not in BUTTON mode. Current mode: " + controlSessionService.getCurrentControlMode())
                    .build());
        }
        boolean success = controlSessionService.sendAngleCommand(jointIndex, angle);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(success)
                .message(success ? "Joint command sent successfully" : "Failed to send joint command")
                .build());
    }

    // POST /api/control-sessions/current/joint-commands/bulk  — replaces /button/angles
    @PostMapping("/current/joint-commands/bulk")
    @Operation(summary = "Send all joint angles in bulk",
               description = "Send angles for all 6 joints at once (BUTTON mode only). " +
                           "Body: array of exactly 6 double values. (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> sendBulkJointCommands(@RequestBody double[] angles) {
        if (controlSessionService.getCurrentControlMode() != ControlMode.BUTTON) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Not in BUTTON mode. Current mode: " + controlSessionService.getCurrentControlMode())
                    .build());
        }
        if (angles == null || angles.length != 6) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .success(false)
                    .message("Angles array must have exactly 6 values")
                    .build());
        }
        boolean success = controlSessionService.sendAllAngles(angles);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(success)
                .message(success ? "Joint commands sent successfully" : "Failed to send joint commands")
                .build());
    }

    // ==================== LEGACY ENDPOINTS (DEPRECATED) ====================

    @Deprecated
    @PostMapping("/camera/start")
    @Operation(summary = "[DEPRECATED] Start AI Camera",
               description = "DEPRECATED — use POST /api/control-sessions with body {\"controlMode\":\"CAMERA\"} instead. (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> startCamera() {
        StartSessionRequest request = StartSessionRequest.builder()
                .controlMode(ControlMode.CAMERA)
                .build();
        SessionStatusResponse response = controlSessionService.startSession(request);
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(response.isSessionActive())
                .message(response.getMessage())
                .data(response)
                .build());
    }

    @Deprecated
    @PostMapping("/camera/stop")
    @Operation(summary = "[DEPRECATED] Stop AI Camera",
               description = "DEPRECATED — use PATCH /api/control-sessions/current/status instead. (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> stopCamera() {
        return stopCurrentSession();
    }

    @Deprecated
    @GetMapping("/camera/status")
    @Operation(summary = "[DEPRECATED] Get Camera status",
               description = "DEPRECATED — use GET /api/control-sessions/current instead. (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> getCameraStatus() {
        return getCurrentSession();
    }
}
