package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.logging;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.example.robotcontrolsystembackend.application.dto.request.logging.UnityLogIngestRequest;
import com.example.robotcontrolsystembackend.application.dto.response.logging.BatchIngestResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LatestRobotStatusResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.LogIngestResultResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.RobotLogResponse;
import com.example.robotcontrolsystembackend.application.service.logging.RobotLogService;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSource;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;
import com.example.robotcontrolsystembackend.infrastructure.persistence.mapper.RobotLogMapper;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotAlertByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotAlertByRobotDayKey;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLatestStatus;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayKey;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayType;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayTypeKey;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogBySession;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogBySessionKey;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotAlertByRobotDayRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotLatestStatusRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotLogByRobotDayRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotLogByRobotDayTypeRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotLogBySessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.CassandraInvalidQueryException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RobotLogServiceImpl implements RobotLogService {

    private static final int MAX_LIMIT = 500;
        private static final Logger log = LoggerFactory.getLogger(RobotLogServiceImpl.class);

    private final RobotLogByRobotDayRepository robotDayRepository;
    private final RobotLogBySessionRepository sessionRepository;
    private final RobotLogByRobotDayTypeRepository robotDayTypeRepository;
    private final RobotAlertByRobotDayRepository alertRepository;
    private final RobotLatestStatusRepository latestStatusRepository;
    private final RobotLogMapper mapper;

    @Override
    public LogIngestResultResponse ingest(UnityLogIngestRequest request) {
        Instant eventTime = request.getEventTime() != null ? request.getEventTime() : Instant.now();
        LocalDate logDate = eventTime.atZone(ZoneOffset.UTC).toLocalDate();
        UUID eventId = request.getEventId() != null ? request.getEventId() : Uuids.timeBased();
        LogSeverity severity = request.getSeverity() != null ? request.getSeverity() : LogSeverity.INFO;
        LogSource source = request.getSource() != null ? request.getSource() : LogSource.UNITY;
        String metadataJson = mapper.toMetadataJson(request.getMetadata());

        List<String> savedTargets = new ArrayList<>();

        RobotLogByRobotDay robotDay = RobotLogByRobotDay.builder()
                .key(RobotLogByRobotDayKey.builder()
                        .robotId(request.getRobotId())
                        .logDate(logDate)
                        .eventTime(eventTime)
                        .eventId(eventId)
                        .build())
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .factoryId(request.getFactoryId())
                .logType(request.getLogType().name())
                .severity(severity.name())
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .command(request.getCommand())
                .source(source.name())
                .message(request.getMessage())
                .traceId(request.getTraceId())
                .metadataJson(metadataJson)
                .build();
        robotDayRepository.save(robotDay);
        savedTargets.add("robot_logs_by_robot_day");

        RobotLogByRobotDayType robotDayType = RobotLogByRobotDayType.builder()
                .key(RobotLogByRobotDayTypeKey.builder()
                        .robotId(request.getRobotId())
                        .logDate(logDate)
                        .logType(request.getLogType().name())
                        .eventTime(eventTime)
                        .eventId(eventId)
                        .build())
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .factoryId(request.getFactoryId())
                .severity(severity.name())
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .command(request.getCommand())
                .source(source.name())
                .message(request.getMessage())
                .traceId(request.getTraceId())
                .metadataJson(metadataJson)
                .build();
        robotDayTypeRepository.save(robotDayType);
        savedTargets.add("robot_logs_by_robot_day_type");

        if (request.getSessionId() != null) {
            RobotLogBySession bySession = RobotLogBySession.builder()
                    .key(RobotLogBySessionKey.builder()
                            .sessionId(request.getSessionId())
                            .eventTime(eventTime)
                            .eventId(eventId)
                            .build())
                    .robotId(request.getRobotId())
                    .userId(request.getUserId())
                    .factoryId(request.getFactoryId())
                    .logType(request.getLogType().name())
                    .severity(severity.name())
                    .status(request.getStatus() != null ? request.getStatus().name() : null)
                    .command(request.getCommand())
                    .source(source.name())
                    .message(request.getMessage())
                    .traceId(request.getTraceId())
                    .metadataJson(metadataJson)
                    .build();
                        try {
                                sessionRepository.save(bySession);
                                savedTargets.add("robot_logs_by_session");
                        } catch (CassandraInvalidQueryException ex) {
                                // Keep ingest available even if session table is still on legacy schema.
                                log.warn("Skip writing robot_logs_by_session due to schema mismatch: {}", ex.getMessage());
                                savedTargets.add("robot_logs_by_session_skipped_schema_mismatch");
                        }
        }

        if (isAlert(severity, request.getLogType())) {
            RobotAlertByRobotDay alert = RobotAlertByRobotDay.builder()
                    .key(RobotAlertByRobotDayKey.builder()
                            .robotId(request.getRobotId())
                            .logDate(logDate)
                            .severity(severity.name())
                            .eventTime(eventTime)
                            .eventId(eventId)
                            .build())
                    .sessionId(request.getSessionId())
                    .userId(request.getUserId())
                    .factoryId(request.getFactoryId())
                    .logType(request.getLogType().name())
                    .status(request.getStatus() != null ? request.getStatus().name() : null)
                    .command(request.getCommand())
                    .source(source.name())
                    .message(request.getMessage())
                    .traceId(request.getTraceId())
                    .metadataJson(metadataJson)
                    .build();
            alertRepository.save(alert);
            savedTargets.add("robot_alerts_by_robot_day");
        }

        if (request.getStatus() != null) {
                        if (shouldUpdateLatestStatus(request.getRobotId(), eventTime)) {
                                RobotLatestStatus latestStatus = RobotLatestStatus.builder()
                                                .robotId(request.getRobotId())
                                                .lastEventTime(eventTime)
                                                .sessionId(request.getSessionId())
                                                .userId(request.getUserId())
                                                .factoryId(request.getFactoryId())
                                                .status(request.getStatus().name())
                                                .severity(severity.name())
                                                .source(source.name())
                                                .message(request.getMessage())
                                                .traceId(request.getTraceId())
                                                .metadataJson(metadataJson)
                                                .build();
                                latestStatusRepository.save(latestStatus);
                                savedTargets.add("robot_latest_status");
                        } else {
                                savedTargets.add("robot_latest_status_skipped_stale_event");
                        }
        }

        return LogIngestResultResponse.builder()
                .eventId(eventId)
                .eventTime(eventTime)
                .robotId(request.getRobotId())
                .savedTargets(savedTargets)
                .build();
    }

    @Override
    public BatchIngestResponse ingestBatch(List<UnityLogIngestRequest> requests) {
        List<BatchIngestResponse.BatchIngestItemResult> items = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < requests.size(); i++) {
            try {
                LogIngestResultResponse result = ingest(requests.get(i));
                success++;
                items.add(BatchIngestResponse.BatchIngestItemResult.builder()
                        .index(i)
                        .success(true)
                        .eventId(result.getEventId().toString())
                        .build());
            } catch (Exception ex) {
                items.add(BatchIngestResponse.BatchIngestItemResult.builder()
                        .index(i)
                        .success(false)
                        .error(ex.getMessage())
                        .build());
            }
        }

        return BatchIngestResponse.builder()
                .total(requests.size())
                .successCount(success)
                .failureCount(requests.size() - success)
                .items(items)
                .build();
    }

    @Override
        public List<RobotLogResponse> getRobotLogs(Long robotId, LocalDate date, LogType type, int limit) {
        int appliedLimit = normalizeLimit(limit, 50);
        if (type == null) {
            return robotDayRepository.findLatestByRobotAndDate(robotId, date, appliedLimit)
                    .stream()
                    .map(mapper::fromRobotDay)
                    .collect(Collectors.toList());
        }
        return robotDayTypeRepository.findByRobotAndDateAndType(robotId, date, type.name(), appliedLimit)
                .stream()
                .map(mapper::fromRobotDayType)
                .collect(Collectors.toList());
    }

    @Override
        public List<RobotLogResponse> getRobotAlerts(Long robotId, LocalDate date, LogSeverity severity, int limit) {
        int appliedLimit = normalizeLimit(limit, 50);
        if (severity == null) {
            return alertRepository.findAlertsByRobotAndDate(robotId, date, appliedLimit)
                    .stream()
                    .map(mapper::fromAlert)
                    .collect(Collectors.toList());
        }
        return alertRepository.findAlertsByRobotAndDateAndSeverity(robotId, date, severity.name(), appliedLimit)
                .stream()
                .map(mapper::fromAlert)
                .collect(Collectors.toList());
    }

    @Override
        public List<RobotLogResponse> getSessionLogs(Long sessionId, int limit) {
        int appliedLimit = normalizeLimit(limit, 100);
                try {
                        return sessionRepository.findBySessionId(sessionId, appliedLimit)
                                        .stream()
                                        .map(mapper::fromSession)
                                        .collect(Collectors.toList());
                } catch (CassandraInvalidQueryException ex) {
                        log.warn("Session log query failed due to schema mismatch: {}", ex.getMessage());
                        return List.of();
                }
    }

    @Override
        public LatestRobotStatusResponse getLatestStatus(Long robotId) {
        RobotLatestStatus status = latestStatusRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("No latest status found for robotId=" + robotId));
        return mapper.fromLatestStatus(status);
    }

    private boolean isAlert(LogSeverity severity, LogType logType) {
        return severity == LogSeverity.WARN
                || severity == LogSeverity.ERROR
                || severity == LogSeverity.CRITICAL
                || logType == LogType.ERROR
                || logType == LogType.WARNING;
    }

    private int normalizeLimit(int requested, int defaultLimit) {
        if (requested <= 0) {
            return defaultLimit;
        }
        return Math.min(requested, MAX_LIMIT);
    }

        private boolean shouldUpdateLatestStatus(Long robotId, Instant incomingEventTime) {
                return latestStatusRepository.findById(robotId)
                                .map(existing -> existing.getLastEventTime() == null || incomingEventTime.isAfter(existing.getLastEventTime()))
                                .orElse(true);
        }
}
