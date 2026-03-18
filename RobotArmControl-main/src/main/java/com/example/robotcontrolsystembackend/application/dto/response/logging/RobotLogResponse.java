package com.example.robotcontrolsystembackend.application.dto.response.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSource;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotLogResponse {
    private UUID eventId;
    private Long robotId;
    private LocalDate logDate;
    private Instant eventTime;
    private Long sessionId;
    private Long userId;
    private Long factoryId;
    private LogType logType;
    private LogSeverity severity;
    private RobotStatus status;
    private String command;
    private LogSource source;
    private String message;
    private String traceId;
    private Object metadata;
    @JsonIgnore
    private String metadataRaw;
}
