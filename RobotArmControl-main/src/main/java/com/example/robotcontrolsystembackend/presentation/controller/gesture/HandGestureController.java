package com.example.robotcontrolsystembackend.presentation.controller.gesture;

import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Hand Gesture Controller for AI Camera
 * - GET operations: All authenticated users (ADMIN, OPERATOR, VIEWER)
 * - POST/PUT/DELETE operations: ADMIN and OPERATOR only
 */
@RestController
@RequestMapping("/api/gestures")
@RequiredArgsConstructor
@Tag(name = "Hand Gesture", description = "APIs for hand gesture recognition from AI Camera")
public class HandGestureController {

    // TODO: Inject HandGestureService when implemented

    @GetMapping
    @Operation(summary = "Get all gestures", description = "Retrieve all registered hand gestures (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getAllGestures() {
        // TODO: Implement with HandGestureService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gestures retrieved successfully")
                .data("List of gestures")
                .build());
    }

    @GetMapping("/{gestureId}")
    @Operation(summary = "Get gesture by ID", description = "Retrieve a specific hand gesture (All roles)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ApiResponse<String>> getGestureById(@PathVariable Long gestureId) {
        // TODO: Implement with HandGestureService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gesture retrieved successfully")
                .data("Gesture " + gestureId)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create gesture", description = "Create a new hand gesture (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createGesture(@RequestBody Object request) {
        // TODO: Implement with HandGestureService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gesture created successfully")
                .data("New gesture")
                .build());
    }

    @PutMapping("/{gestureId}")
    @Operation(summary = "Update gesture", description = "Update an existing hand gesture (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> updateGesture(
            @PathVariable Long gestureId,
            @RequestBody Object request) {
        // TODO: Implement with HandGestureService
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gesture updated successfully")
                .data("Updated gesture " + gestureId)
                .build());
    }

    @DeleteMapping("/{gestureId}")
    @Operation(summary = "Delete gesture", description = "Delete a hand gesture (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGesture(@PathVariable Long gestureId) {
        // TODO: Implement with HandGestureService
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Gesture deleted successfully")
                .build());
    }

    // POST /api/gestures/recognitions  — replaces /recognize verb
    @PostMapping("/recognitions")
    @Operation(summary = "Create gesture recognition", description = "Submit AI Camera feed data to recognize a hand gesture (ADMIN, OPERATOR only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<String>> createRecognition(@RequestBody Object cameraData) {
        // TODO: Implement with AI Camera integration
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Gesture recognized successfully")
                .data("Recognized gesture result")
                .build());
    }
}
