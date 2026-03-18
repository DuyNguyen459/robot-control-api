package com.example.robotcontrolsystembackend.application.dto.request.logging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUnityLogIngestRequest {

    @NotEmpty(message = "logs must not be empty")
    @Valid
    private List<UnityLogIngestRequest> logs;
}
