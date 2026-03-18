package com.example.robotcontrolsystembackend.presentation.controller;

import com.example.robotcontrolsystembackend.application.service.RobotMessagingUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Testing endpoints for robot messaging")
public class RobotTestController {

    private final RobotMessagingUseCase messagingService;

    @Data
    @Schema(name = "SendAnglesRequest", description = "Request body to send angles to a robot device")
    public static class SendAnglesRequest {
        @Schema(description = "Device identifier (e.g. robot-1)", example = "robot-1")
        public String deviceId;

        @Schema(description = "Array of joint angles in degrees", example = "[0,10,20,30,40,50]")
        public double[] angles;
    }

    @Operation(summary = "Send AI angles to a robot",
            responses = {
                    @ApiResponse(responseCode = "200", description = "sent"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                    @ApiResponse(responseCode = "404", description = "device not connected", content = @Content)
            })
    @PostMapping("/send")
    public ResponseEntity<?> sendAngles(@RequestBody SendAnglesRequest req) {
        if (req == null || req.deviceId == null || req.angles == null) {
            return ResponseEntity.badRequest().body("deviceId and angles required");
        }

        boolean sent = messagingService.sendAiAngles(req.deviceId, req.angles);
        if (sent) return ResponseEntity.ok().body("sent");
        return ResponseEntity.status(404).body("device not connected");
    }
}