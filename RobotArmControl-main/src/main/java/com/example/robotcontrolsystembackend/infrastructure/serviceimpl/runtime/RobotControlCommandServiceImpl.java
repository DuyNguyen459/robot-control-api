package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.RobotControlCommandRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.RobotControlDispatchResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.RobotControlCommandService;
import com.example.robotcontrolsystembackend.grpc.RobotControlCommand;
import com.example.robotcontrolsystembackend.infrastructure.grpc.RobotControlStreamHub;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RobotControlCommandServiceImpl implements RobotControlCommandService {

    private final RobotControlStreamHub streamHub;
    private final ObjectMapper objectMapper;

    @Value("${robot.control.broadcast-websocket:false}")
    private boolean broadcastLegacyWebsocket;

    @Value("${robot.control.gripper.min:0}")
    private int minGripper;

    @Value("${robot.control.gripper.max:1}")
    private int maxGripper;

    // Optional legacy fallback: keep disabled by default to avoid duplicated traffic.
    private final com.example.robotcontrolsystembackend.config.websocket.RobotControlWebSocketHandler webSocketHandler;

    @Override
    public RobotControlDispatchResponse dispatchCommand(RobotControlCommandRequest request, String source) {
        List<Double> joints = request.getJointAngles();
        validateAngles(joints);
        validateGripper(request.getGripper());

        String timestamp = normalizeTimestamp(request.getTimestamp());

        RobotControlCommand.Builder builder = RobotControlCommand.newBuilder()
                .setRobotId(request.getRobotId())
                .setTimestamp(timestamp)
                .setSource(source == null ? "api" : source);

        for (Double angle : joints) {
            builder.addJointAngles(angle);
        }

        if (request.getGripper() != null) {
            builder.setHasGripper(true);
            builder.setGripper(request.getGripper());
        }

        RobotControlCommand command = builder.build();
        int delivered = streamHub.publish(command);

        if (broadcastLegacyWebsocket) {
            broadcastLegacyPayload(request, timestamp);
        }

        String message = delivered > 0
                ? "Robot control command dispatched"
                : "Command accepted, no Unity gRPC subscribers currently connected";

        return RobotControlDispatchResponse.builder()
                .robotId(request.getRobotId())
                .subscribersReached(delivered)
                .timestamp(timestamp)
                .message(message)
                .build();
    }

    private void validateAngles(List<Double> joints) {
        if (joints == null || joints.size() != 6) {
            throw new IllegalArgumentException("jointAngles must contain exactly 6 values");
        }

        for (Double value : joints) {
            if (value == null || value.isNaN() || value.isInfinite()) {
                throw new IllegalArgumentException("jointAngles values must be finite numbers");
            }
        }
    }

    private void validateGripper(Integer gripper) {
        if (gripper == null) {
            return;
        }
        if (gripper < minGripper || gripper > maxGripper) {
            throw new IllegalArgumentException("gripper must be in range [" + minGripper + ", " + maxGripper + "]");
        }
    }

    private String normalizeTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Instant.now().toString();
        }
        return timestamp;
    }

    private void broadcastLegacyPayload(RobotControlCommandRequest request, String timestamp) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("type", "ai_angles");
            payload.put("deviceId", String.valueOf(request.getRobotId()));
            payload.put("timestamp", timestamp);

            ArrayNode angles = payload.putArray("angles");
            for (Double value : request.getJointAngles()) {
                angles.add(value);
            }

            if (request.getGripper() != null) {
                payload.put("gripper", request.getGripper());
            }

            webSocketHandler.broadcastMessage(payload.toString());
        } catch (Exception ex) {
            log.warn("Legacy WebSocket fallback failed: {}", ex.getMessage());
        }
    }
}
