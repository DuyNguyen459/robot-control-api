package com.example.robotcontrolsystembackend.presentation.controller.gesture;

import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Robot Command Controller for AI Camera
 * Manages robot arm commands triggered by hand gestures
 * - GET operations: All authenticated users (ADMIN, OPERATOR, VIEWER)
 * - POST/PUT/DELETE operations: ADMIN and OPERATOR only
 */
@RestController
@RequestMapping("/api/robot-commands")
@RequiredArgsConstructor
@Tag(name = "Robot Command", description = "APIs for robot arm command management")
public class RobotCommandController {

    // TODO: Inject RobotCommandService when implemented

    @GetMapping
    @Operation(summary = "Get all commands", description = "Retrieve all registered robot commands (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getAllCommands() {
        // TODO: Implement with RobotCommandService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Commands retrieved successfully")
                .data("List of commands")
                .build());
    }

    @GetMapping("/{commandId}")
    @Operation(summary = "Get command by ID", description = "Retrieve a specific robot command (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getCommandById(@PathVariable Long commandId) {
        // TODO: Implement with RobotCommandService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Command retrieved successfully")
                .data("Command " + commandId)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create command", description = "Create a new robot command (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createCommand(@RequestBody Object request) {
        // TODO: Implement with RobotCommandService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Command created successfully")
                .data("New command")
                .build());
    }

    @PutMapping("/{commandId}")
    @Operation(summary = "Update command", description = "Update an existing robot command (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateCommand(
            @PathVariable Long commandId,
            @RequestBody Object request) {
        // TODO: Implement with RobotCommandService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Command updated successfully")
                .data("Updated command " + commandId)
                .build());
    }

    @DeleteMapping("/{commandId}")
    @Operation(summary = "Delete command", description = "Delete a robot command (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCommand(@PathVariable Long commandId) {
        // TODO: Implement with RobotCommandService
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Command deleted successfully")
                .build());
    }

    // POST /api/robot-commands/{commandId}/executions  — replaces /{commandId}/execute verb
    @PostMapping("/{commandId}/executions")
    @Operation(summary = "Execute robot command", description = "Execute a specific robot command and record the execution result (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createCommandExecution(@PathVariable Long commandId) {
        // TODO: Implement with robot arm integration
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Command executed successfully")
                .data("Execution result for command " + commandId)
                .build());
    }
}
