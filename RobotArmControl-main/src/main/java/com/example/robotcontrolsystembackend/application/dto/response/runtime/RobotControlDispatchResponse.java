package com.example.robotcontrolsystembackend.application.dto.response.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotControlDispatchResponse {
    private Long robotId;
    private int subscribersReached;
    private String timestamp;
    private String message;
}
