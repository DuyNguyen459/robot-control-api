package com.example.robotcontrolsystembackend.presentation.controller.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.StartSessionRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.SessionStatusResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.ControlSessionService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Robot Runtime Controller
 * Real-time robot arm control through gesture recognition
 *
 * Endpoints:
 *   GET    /api/robots/status               — get robot arm status
 *   GET    /api/robots/joints               — get all joint positions
 *   POST   /api/robots/sessions             — start robot session (replaces /start)
 *   PATCH  /api/robots/sessions/current/status — stop session (replaces POST /stop)
 *   POST   /api/robots/emergency-stop       — safety override (acceptable action exception)
 *   PATCH  /api/robots/joints/{jointIndex}  — update joint angle (replaces POST /move-joint)
 *   POST   /api/robots/commands             — issue gesture/AI command (replaces /gesture-control)
 *   POST   /api/robots/calibrations         — start calibration (replaces /calibrate)
 */
@RestController
@RequestMapping("/api/robots")  // Fixed: was "/api/runtime"
@RequiredArgsConstructor
@Tag(name = "Robot Runtime", description = "APIs for real-time robot arm control")
public class RobotRuntimeController {

    private final ControlSessionService controlSessionService;

    @GetMapping("/status")
    @Operation(summary = "Get robot status", description = "Get current robot arm status (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getRobotStatus() {
        // TODO: Implement with RobotRuntimeService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Robot status retrieved successfully")
                .data("Robot status: READY")
                .build());
    }

    @GetMapping("/joints")
    @Operation(summary = "Get joint positions", description = "Get current positions of all robot joints (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getJointPositions() {
        // TODO: Implement with RobotRuntimeService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Joint positions retrieved successfully")
                .data("Joint positions")
                .build());
    }

    // POST /api/robots/sessions  — replaces POST /start
    @PostMapping("/sessions")
    @Operation(summary = "Start robot session", description = "Start a new robot arm control session (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
        public ResponseEntity<ApiResponse<SessionStatusResponse>> createRobotSession(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(defaultValue = "BUTTON") ControlMode controlMode) {
        SessionStatusResponse response = controlSessionService.startSession(
            StartSessionRequest.builder()
                .controlMode(controlMode)
                .deviceId(deviceId)
                .build()
        );
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
            .success(response.isSessionActive())
            .message(response.getMessage())
            .data(response)
                .build());
    }

    // PATCH /api/robots/sessions/current/status  — replaces POST /stop
    @PatchMapping("/sessions/current/status")
    @Operation(summary = "Stop robot session", description = "Stop the current robot arm control session (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> stopRobotSession() {
        SessionStatusResponse response = controlSessionService.stopSession();
        return ResponseEntity.ok(ApiResponse.<SessionStatusResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }

    // POST /api/robots/emergency-stop  — safety override; action exception is acceptable
    @PostMapping("/emergency-stop")
    @Operation(summary = "Emergency stop", description = "Emergency stop all robot operations — safety override (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> emergencyStop() {
        // TODO: Implement with RobotRuntimeService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Emergency stop executed")
                .data("All operations stopped")
                .build());
    }

    // PATCH /api/robots/joints/{jointIndex}  — replaces POST /move-joint
    @PatchMapping("/joints/{jointIndex}")
    @Operation(summary = "Update joint position", description = "Update the angle of a specific robot joint. Body: {\"angle\": 45.0} (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateJointPosition(
            @PathVariable int jointIndex,
            @RequestBody Object jointRequest) {
        // TODO: Implement with RobotRuntimeService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Joint position updated successfully")
                .data("Joint " + jointIndex + " updated")
                .build());
    }

    // POST /api/robots/commands  — replaces POST /gesture-control
    @PostMapping("/commands")
    @Operation(summary = "Issue robot command", description = "Issue a command to the robot arm, e.g., via AI Camera gesture recognition. Body: {\"type\":\"GESTURE\", \"data\":{...}} (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createRobotCommand(@RequestBody Object commandData) {
        // TODO: Implement with AI Camera integration
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Robot command issued successfully")
                .data("Command result")
                .build());
    }

    // POST /api/robots/calibrations  — replaces POST /calibrate
    @PostMapping("/calibrations")
    @Operation(summary = "Start robot calibration", description = "Start a robot arm calibration process (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> createCalibration() {
        // TODO: Implement with RobotRuntimeService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Robot calibration started successfully")
                .data("Calibration result")
                .build());
    }
}
