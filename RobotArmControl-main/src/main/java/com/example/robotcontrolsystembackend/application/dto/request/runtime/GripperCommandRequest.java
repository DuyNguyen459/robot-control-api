package com.example.robotcontrolsystembackend.application.dto.request.runtime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GripperCommandRequest {

    @Positive(message = "deviceId must be positive")
    private Long deviceId;

    @NotBlank(message = "action is required")
    private String action;
}
