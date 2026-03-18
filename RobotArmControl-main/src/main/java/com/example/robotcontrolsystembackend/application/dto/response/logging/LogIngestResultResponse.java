package com.example.robotcontrolsystembackend.application.dto.response.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogIngestResultResponse {
    private UUID eventId;
    private Instant eventTime;
    private Long robotId;
    private List<String> savedTargets;
}
