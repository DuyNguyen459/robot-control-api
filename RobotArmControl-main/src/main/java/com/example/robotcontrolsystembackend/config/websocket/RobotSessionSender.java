package com.example.robotcontrolsystembackend.config.websocket;

public interface RobotSessionSender {
    boolean sendToDevice(String deviceId, String message);
}
