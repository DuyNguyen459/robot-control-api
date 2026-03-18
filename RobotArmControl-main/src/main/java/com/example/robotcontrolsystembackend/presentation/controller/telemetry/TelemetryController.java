package com.example.robotcontrolsystembackend.presentation.controller.telemetry;

import com.example.robotcontrolsystembackend.application.service.telemetry.TelemetryService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.model.telemetry.RobotTelemetry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetry", description = "APIs for robot telemetry data (Astra DB)")
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Save telemetry data")
    public ResponseEntity<ApiResponse<RobotTelemetry>> saveTelemetry(
            @RequestBody RobotTelemetry telemetry) {
        return ResponseEntity.ok(ApiResponse.<RobotTelemetry>builder()
                .success(true)
                .message("Telemetry saved")
                .data(telemetryService.saveTelemetry(telemetry))
                .build());
    }

    @GetMapping("/{deviceId}/latest")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get latest telemetry for a device")
    public ResponseEntity<ApiResponse<List<RobotTelemetry>>> getLatest(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "50") int count) {
        return ResponseEntity.ok(ApiResponse.<List<RobotTelemetry>>builder()
                .success(true)
                .message("Telemetry retrieved")
                .data(telemetryService.getLatestTelemetry(deviceId, count))
                .build());
    }

    @GetMapping("/{deviceId}/history")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get telemetry history in time range")
    public ResponseEntity<ApiResponse<List<RobotTelemetry>>> getHistory(
            @PathVariable Long deviceId,
            @RequestParam String from,
            @RequestParam String to) {
        Instant start = Instant.parse(from);
        Instant end = Instant.parse(to);
        return ResponseEntity.ok(ApiResponse.<List<RobotTelemetry>>builder()
                .success(true)
                .message("Telemetry history retrieved")
                .data(telemetryService.getTelemetryHistory(deviceId, start, end))
                .build());
    }
}
