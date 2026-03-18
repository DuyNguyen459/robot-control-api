package com.example.robotcontrolsystembackend.presentation.controller.gesture;

import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Gesture to Command Mapping Controller for AI Camera
 * Maps hand gestures to robot commands
 * - GET operations: All authenticated users (ADMIN, OPERATOR, VIEWER)
 * - POST/PUT/DELETE operations: ADMIN and OPERATOR only
 */
@RestController
@RequestMapping("/api/gesture-mappings")
@RequiredArgsConstructor
@Tag(name = "Gesture Command Mapping", description = "APIs for mapping gestures to robot commands")
public class GestureCommandMapController {

    // TODO: Inject GestureCommandMapService when implemented

    @GetMapping
    @Operation(summary = "Get all mappings", description = "Retrieve all gesture-to-command mappings (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getAllMappings() {
        // TODO: Implement with GestureCommandMapService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Mappings retrieved successfully")
                .data("List of mappings")
                .build());
    }

    @GetMapping("/{mappingId}")
    @Operation(summary = "Get mapping by ID", description = "Retrieve a specific gesture-command mapping (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getMappingById(@PathVariable Long mappingId) {
        // TODO: Implement with GestureCommandMapService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Mapping retrieved successfully")
                .data("Mapping " + mappingId)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create mapping", description = "Create a new gesture-to-command mapping (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createMapping(@RequestBody Object request) {
        // TODO: Implement with GestureCommandMapService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Mapping created successfully")
                .data("New mapping")
                .build());
    }

    @PutMapping("/{mappingId}")
    @Operation(summary = "Update mapping", description = "Update an existing gesture-command mapping (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateMapping(
            @PathVariable Long mappingId,
            @RequestBody Object request) {
        // TODO: Implement with GestureCommandMapService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Mapping updated successfully")
                .data("Updated mapping " + mappingId)
                .build());
    }

    @DeleteMapping("/{mappingId}")
    @Operation(summary = "Delete mapping", description = "Delete a gesture-command mapping (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMapping(@PathVariable Long mappingId) {
        // TODO: Implement with GestureCommandMapService
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Mapping deleted successfully")
                .build());
    }

    // POST /api/gesture-mappings/executions  — replaces /execute verb
    @PostMapping("/executions")
    @Operation(summary = "Execute gesture mapping", description = "Trigger robot command execution based on a recognized gesture. Body: gesture data from AI Camera (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createExecution(@RequestBody Object gestureData) {
        // TODO: Implement with robot control integration
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gesture mapping executed successfully")
                .data("Execution result")
                .build());
    }
}
