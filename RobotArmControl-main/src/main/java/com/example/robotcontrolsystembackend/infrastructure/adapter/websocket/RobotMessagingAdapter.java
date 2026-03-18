package com.example.robotcontrolsystembackend.infrastructure.adapter.websocket;

import com.example.robotcontrolsystembackend.application.service.RobotMessagingUseCase;
import com.example.robotcontrolsystembackend.config.websocket.RobotSessionSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RobotMessagingAdapter implements RobotMessagingUseCase {

    private final RobotSessionSender wsHandler;
    private final ObjectMapper objectMapper;

    @Override
    public boolean sendAiAngles(String deviceId, double[] angles) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "ai_angles");
        root.put("deviceId", deviceId);
        ArrayNode arr = root.putArray("angles");
        for (double a : angles) arr.add(a);
        return wsHandler.sendToDevice(deviceId, root.toString());
    }

    @Override
    public boolean sendRawToDevice(String deviceId, String json) {
        return wsHandler.sendToDevice(deviceId, json);
    }
}
