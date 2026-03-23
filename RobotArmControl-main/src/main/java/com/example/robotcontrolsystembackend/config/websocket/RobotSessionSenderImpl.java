package com.example.robotcontrolsystembackend.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RobotSessionSenderImpl implements RobotSessionSender {

    private final RobotControlWebSocketHandler webSocketHandler;

    @Override
    public boolean sendToDevice(String deviceId, String message) {
        return webSocketHandler.sendToDevice(deviceId, message);
    }
}