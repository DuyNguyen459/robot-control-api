package com.example.robotcontrolsystembackend.application.dto.request.runtime;

import jakarta.validation.constraints.NotNull;
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
public class AiAnglesRequest {

    @NotNull(message = "angles is required")
    @Size(min = 6, max = 6, message = "angles must have exactly 6 values")
    private List<Double> angles;
}
