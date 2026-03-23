package com.example.robotcontrolsystembackend.application.dto.request.runtime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotControlCommandRequest {

    @NotNull(message = "robotId is required")
    @Positive(message = "robotId must be positive")
    private Long robotId;

    @NotNull(message = "jointAngles is required")
    @Size(min = 6, max = 6, message = "jointAngles must contain exactly 6 values")
    private List<Double> jointAngles;

    // 0 = release, 1 = grab
    private Integer gripper;

    // ISO-8601 string from FE/AI camera; backend fills current time if missing.
    private String timestamp;
}
