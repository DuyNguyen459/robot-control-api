package com.example.robotcontrolsystembackend.config.websocket;

import org.springframework.stereotype.Component;

public class RobotSessionSenderImpl implements RobotSessionSender {
    @Override
    public boolean sendToDevice(String deviceId, String message) {
        // Thực hiện gửi message tới device ở đây
        return true;
    }
}