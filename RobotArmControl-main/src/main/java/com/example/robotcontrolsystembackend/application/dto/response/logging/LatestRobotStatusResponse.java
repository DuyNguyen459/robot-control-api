package com.example.robotcontrolsystembackend.application.dto.response.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSeverity;
import com.example.robotcontrolsystembackend.domain.enumtype.LogSource;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LatestRobotStatusResponse {
    private Long robotId;
    private Instant lastEventTime;
    private Long sessionId;
    private Long userId;
    private Long factoryId;
    private RobotStatus status;
    private LogSeverity severity;
    private LogSource source;
    private String message;
    private String traceId;
    private Object metadata;
    @JsonIgnore
    private String metadataRaw;
}
