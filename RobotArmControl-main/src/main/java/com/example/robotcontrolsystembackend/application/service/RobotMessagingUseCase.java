package com.example.robotcontrolsystembackend.application.service;

public interface RobotMessagingUseCase {
    boolean sendAiAngles(String deviceId, double[] angles);
    boolean sendRawToDevice(String deviceId, String json);
}
