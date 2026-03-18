package com.example.robotcontrolsystembackend.presentation.controller.logging;

import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Control Log Controller for AI Camera
 * Provides access to control logs and robot arm operation history
 * - GET operations: All authenticated users (ADMIN, OPERATOR, VIEWER) can view logs
 * - POST operations: ADMIN and OPERATOR only (logs are created during control operations)
 * - DELETE operations: ADMIN only
 */
@RestController
@RequestMapping("/api/control-logs")
@RequiredArgsConstructor
@Tag(name = "Control Log", description = "APIs for viewing control logs and operation history")
public class ControlLogController {

    // TODO: Inject ControlLogService when implemented

    // GET /api/control-logs?deviceId=&userId=&sessionId=&type=&command=&sortBy=&sortDir=&startTime=&endTime=&page=&size=
    @GetMapping
    @Operation(summary = "Get control logs", description = "Retrieve control logs with optional filters for device, user, session, type (GESTURE/BUTTON), time range, and pagination (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String command,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        // TODO: Implement with ControlLogService — pass all filters through
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Control logs retrieved successfully")
                .data("List of control logs")
                .build());
    }

    @GetMapping("/{logId}")
    @Operation(summary = "Get log by ID", description = "Retrieve a specific control log entry (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getLogById(@PathVariable Long logId) {
        // TODO: Implement with ControlLogService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Control log retrieved successfully")
                .data("Log entry " + logId)
                .build());
    }

    // GET /api/control-logs/stats?startTime=&endTime=
    // Note: will be moved to GET /api/statistics/control-logs in a future refactor
    @GetMapping("/stats")
    @Operation(summary = "Get log statistics", description = "Retrieve control log statistics and analytics (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getLogStats(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        // TODO: Implement with ControlLogService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Log statistics retrieved successfully")
                .data("Log statistics")
                .build());
    }

    @DeleteMapping("/{logId}")
    @Operation(summary = "Delete log entry", description = "Delete a specific control log entry (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable Long logId) {
        // TODO: Implement with ControlLogService
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Log entry deleted successfully")
                .build());
    }

    // DELETE /api/control-logs/bulk?before=2025-01-01T00:00:00 — replaces /clear verb
    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete old logs", description = "Delete all logs before a specified ISO datetime (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkDeleteLogs(@RequestParam LocalDateTime before) {
        // TODO: Implement with ControlLogService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Old logs deleted successfully")
                .data("Deleted logs before " + before)
                .build());
    }
}
