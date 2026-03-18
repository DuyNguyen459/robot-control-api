package com.example.robotcontrolsystembackend.presentation.controller.logging;

import com.example.robotcontrolsystembackend.application.dto.request.logging.BatchUnityLogIngestRequest;
import com.example.robotcontrolsystembackend.application.dto.request.logging.UnityLogIngestRequest;
import com.example.robotcontrolsystembackend.application.dto.response.logging.BatchIngestResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LatestRobotStatusResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LogIngestResultResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.RobotLogResponse;
import com.example.robotcontrolsystembackend.application.service.logging.RobotLogService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Robot Logs", description = "APIs for ingesting and querying robot logs in Astra DB")
public class RobotLogController {

    private final RobotLogService robotLogService;

    @PostMapping("/ingest")
    @Operation(summary = "Ingest one log", description = "Receive one log event from Unity and persist to Cassandra tables")
    public ResponseEntity<ApiResponse<LogIngestResultResponse>> ingest(@Valid @RequestBody UnityLogIngestRequest request) {
        LogIngestResultResponse response = robotLogService.ingest(request);
        return ResponseEntity.ok(ApiResponse.ok("Log ingested", response));
    }

    @PostMapping("/ingest/batch")
    @Operation(summary = "Batch ingest logs", description = "Receive a batch of logs and return partial success/failure details")
    public ResponseEntity<ApiResponse<BatchIngestResponse>> ingestBatch(@Valid @RequestBody BatchUnityLogIngestRequest request) {
        BatchIngestResponse response = robotLogService.ingestBatch(request.getLogs());
        return ResponseEntity.ok(ApiResponse.ok("Batch ingest completed", response));
    }

    @GetMapping("/robots/{robotId}")
    @Operation(summary = "Get latest logs by robot/day", description = "Returns latest logs for robot/day, optionally filtered by type")
    public ResponseEntity<ApiResponse<List<RobotLogResponse>>> getRobotLogs(
            @PathVariable Long robotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) LogType type,
            @RequestParam(defaultValue = "50") int limit) {
        List<RobotLogResponse> response = robotLogService.getRobotLogs(robotId, date, type, limit);
        return ResponseEntity.ok(ApiResponse.ok("Robot logs retrieved", response));
    }

    @GetMapping("/robots/{robotId}/today")
    @Operation(summary = "Get today's logs by robot", description = "Returns latest logs for the current UTC day, optionally filtered by type")
    public ResponseEntity<ApiResponse<List<RobotLogResponse>>> getRobotLogsToday(
            @PathVariable Long robotId,
            @RequestParam(required = false) LogType type,
            @RequestParam(defaultValue = "50") int limit) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        List<RobotLogResponse> response = robotLogService.getRobotLogs(robotId, todayUtc, type, limit);
        return ResponseEntity.ok(ApiResponse.ok("Today's robot logs retrieved", response));
    }

    @GetMapping("/robots/{robotId}/alerts")
    @Operation(summary = "Get alerts by robot/day", description = "Returns warning/error logs for robot/day, optionally filtered by severity")
    public ResponseEntity<ApiResponse<List<RobotLogResponse>>> getRobotAlerts(
            @PathVariable Long robotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) LogSeverity severity,
            @RequestParam(defaultValue = "50") int limit) {
        List<RobotLogResponse> response = robotLogService.getRobotAlerts(robotId, date, severity, limit);
        return ResponseEntity.ok(ApiResponse.ok("Robot alerts retrieved", response));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get logs by session", description = "Returns logs for one control session")
    public ResponseEntity<ApiResponse<List<RobotLogResponse>>> getSessionLogs(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "100") int limit) {
        List<RobotLogResponse> response = robotLogService.getSessionLogs(sessionId, limit);
        return ResponseEntity.ok(ApiResponse.ok("Session logs retrieved", response));
    }

    @GetMapping("/robots/{robotId}/latest-status")
    @Operation(summary = "Get latest robot status", description = "Returns latest known status snapshot of the robot")
    public ResponseEntity<ApiResponse<LatestRobotStatusResponse>> getLatestStatus(@PathVariable Long robotId) {
        LatestRobotStatusResponse response = robotLogService.getLatestStatus(robotId);
        return ResponseEntity.ok(ApiResponse.ok("Latest status retrieved", response));
    }
}
