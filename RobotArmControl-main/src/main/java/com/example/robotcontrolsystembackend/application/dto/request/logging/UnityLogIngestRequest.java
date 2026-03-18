package com.example.robotcontrolsystembackend.application.dto.request.logging;

import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSource;
import com.example.robotcontrolsystembackend.domain.enumtype.LogType;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnityLogIngestRequest {

    private UUID eventId;

    @NotNull(message = "robotId is required")
    private Long robotId;

    private Long sessionId;

    private Long userId;

    private Long factoryId;

    @NotNull(message = "logType is required")
    private LogType logType;

    private LogSeverity severity;

    private RobotStatus status;

    private String command;

    @NotBlank(message = "message is required")
    private String message;

    private Instant eventTime;

    private LogSource source;

    private String traceId;

    private Object metadata;
}
