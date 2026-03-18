package com.example.robotcontrolsystembackend.application.service.logging;

import com.example.robotcontrolsystembackend.application.dto.request.logging.UnityLogIngestRequest;
import com.example.robotcontrolsystembackend.application.dto.response.logging.BatchIngestResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LatestRobotStatusResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LogIngestResultResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.RobotLogResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;

import java.time.LocalDate;
import java.util.List;

public interface RobotLogService {
    LogIngestResultResponse ingest(UnityLogIngestRequest request);

    BatchIngestResponse ingestBatch(List<UnityLogIngestRequest> requests);

    List<RobotLogResponse> getRobotLogs(Long robotId, LocalDate date, LogType type, int limit);

    List<RobotLogResponse> getRobotAlerts(Long robotId, LocalDate date, LogSeverity severity, int limit);

    List<RobotLogResponse> getSessionLogs(Long sessionId, int limit);

    LatestRobotStatusResponse getLatestStatus(Long robotId);
}
