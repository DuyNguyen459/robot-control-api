package com.example.robotcontrolsystembackend.infrastructure.persistence.mapper;

import com.example.robotcontrolsystembackend.application.dto.response.logging.LatestRobotStatusResponse;
import com.example.robotcontrolsystembackend.application.dto.response.logging.RobotLogResponse;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSource;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotStatus;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotAlertByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLatestStatus;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayType;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogBySession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RobotLogMapper {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public String toMetadataJson(Object metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("metadata must be valid JSON", ex);
        }
    }

    public Object parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return null;
        }
        try {
            // Parse into plain Java types (Map/List/primitive) for clean API JSON output.
            return objectMapper.readValue(metadataJson, Object.class);
        } catch (Exception ex) {
            return metadataJson;
        }
    }

    public RobotLogResponse fromRobotDay(RobotLogByRobotDay row) {
        return RobotLogResponse.builder()
                .eventId(row.getKey().getEventId())
                .robotId(row.getKey().getRobotId())
                .logDate(row.getKey().getLogDate())
                .eventTime(row.getKey().getEventTime())
                .sessionId(row.getSessionId())
                .userId(row.getUserId())
                .factoryId(row.getFactoryId())
                .logType(enumOrNull(LogType.class, row.getLogType()))
                .severity(enumOrNull(LogSeverity.class, row.getSeverity()))
                .status(enumOrNull(RobotStatus.class, row.getStatus()))
                .command(row.getCommand())
                .source(enumOrNull(LogSource.class, row.getSource()))
                .message(row.getMessage())
                .traceId(row.getTraceId())
                .metadata(parseMetadata(row.getMetadataJson()))
                .metadataRaw(row.getMetadataJson())
                .build();
    }

    public RobotLogResponse fromRobotDayType(RobotLogByRobotDayType row) {
        return RobotLogResponse.builder()
                .eventId(row.getKey().getEventId())
                .robotId(row.getKey().getRobotId())
                .logDate(row.getKey().getLogDate())
                .eventTime(row.getKey().getEventTime())
                .sessionId(row.getSessionId())
                .userId(row.getUserId())
                .factoryId(row.getFactoryId())
                .logType(enumOrNull(LogType.class, row.getKey().getLogType()))
                .severity(enumOrNull(LogSeverity.class, row.getSeverity()))
                .status(enumOrNull(RobotStatus.class, row.getStatus()))
                .command(row.getCommand())
                .source(enumOrNull(LogSource.class, row.getSource()))
                .message(row.getMessage())
                .traceId(row.getTraceId())
                .metadata(parseMetadata(row.getMetadataJson()))
                .metadataRaw(row.getMetadataJson())
                .build();
    }

    public RobotLogResponse fromSession(RobotLogBySession row) {
        return RobotLogResponse.builder()
                .eventId(row.getKey().getEventId())
                .robotId(row.getRobotId())
                .logDate(LocalDate.from(row.getKey().getEventTime().atZone(java.time.ZoneOffset.UTC)))
                .eventTime(row.getKey().getEventTime())
                .sessionId(row.getKey().getSessionId())
                .userId(row.getUserId())
                .factoryId(row.getFactoryId())
                .logType(enumOrNull(LogType.class, row.getLogType()))
                .severity(enumOrNull(LogSeverity.class, row.getSeverity()))
                .status(enumOrNull(RobotStatus.class, row.getStatus()))
                .command(row.getCommand())
                .source(enumOrNull(LogSource.class, row.getSource()))
                .message(row.getMessage())
                .traceId(row.getTraceId())
                .metadata(parseMetadata(row.getMetadataJson()))
                .metadataRaw(row.getMetadataJson())
                .build();
    }

    public RobotLogResponse fromAlert(RobotAlertByRobotDay row) {
        return RobotLogResponse.builder()
                .eventId(row.getKey().getEventId())
                .robotId(row.getKey().getRobotId())
                .logDate(row.getKey().getLogDate())
                .eventTime(row.getKey().getEventTime())
                .sessionId(row.getSessionId())
                .userId(row.getUserId())
                .factoryId(row.getFactoryId())
                .logType(enumOrNull(LogType.class, row.getLogType()))
                .severity(enumOrNull(LogSeverity.class, row.getKey().getSeverity()))
                .status(enumOrNull(RobotStatus.class, row.getStatus()))
                .command(row.getCommand())
                .source(enumOrNull(LogSource.class, row.getSource()))
                .message(row.getMessage())
                .traceId(row.getTraceId())
                .metadata(parseMetadata(row.getMetadataJson()))
                .metadataRaw(row.getMetadataJson())
                .build();
    }

    public LatestRobotStatusResponse fromLatestStatus(RobotLatestStatus status) {
        return LatestRobotStatusResponse.builder()
                .robotId(status.getRobotId())
                .lastEventTime(status.getLastEventTime())
                .sessionId(status.getSessionId())
                .userId(status.getUserId())
                .factoryId(status.getFactoryId())
                .status(enumOrNull(RobotStatus.class, status.getStatus()))
                .severity(enumOrNull(LogSeverity.class, status.getSeverity()))
                .source(enumOrNull(LogSource.class, status.getSource()))
                .message(status.getMessage())
                .traceId(status.getTraceId())
                .metadata(parseMetadata(status.getMetadataJson()))
                .metadataRaw(status.getMetadataJson())
                .build();
    }

    private <T extends Enum<T>> T enumOrNull(Class<T> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
