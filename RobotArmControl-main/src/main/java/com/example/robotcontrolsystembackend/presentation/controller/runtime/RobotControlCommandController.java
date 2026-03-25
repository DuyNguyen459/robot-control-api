package com.example.robotcontrolsystembackend.presentation.controller.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.RobotControlCommandRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.RobotControlDispatchResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.RobotControlCommandService;
import com.example.robotcontrolsystembackend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/robot-control")
@RequiredArgsConstructor
@Tag(name = "Robot Control Bridge", description = "REST ingress for robot control commands, forwarded to Unity over gRPC stream")
public class RobotControlCommandController {

    private final RobotControlCommandService robotControlCommandService;

    @PostMapping("/commands")
    @Operation(
            summary = "Dispatch robot control command",
            description = "Receives robot control from FE/AI-camera, validates payload, then forwards to Unity subscribers over gRPC"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<RobotControlDispatchResponse>> dispatch(
            @Valid @RequestBody RobotControlCommandRequest request
    ) {

        // Log chi tiết payload nhận được để debug
        System.out.println("[DEBUG] RobotControlCommandRequest payload: robotId=" + request.getRobotId()
                + ", jointAngles=" + request.getJointAngles()
                + ", gripper=" + request.getGripper()
                + ", timestamp=" + request.getTimestamp());

        RobotControlDispatchResponse response = robotControlCommandService.dispatchCommand(request, "rest-api");

        return ResponseEntity.ok(ApiResponse.<RobotControlDispatchResponse>builder()
                .success(true)
                .message(response.getMessage())
                .data(response)
                .build());
    }
}
