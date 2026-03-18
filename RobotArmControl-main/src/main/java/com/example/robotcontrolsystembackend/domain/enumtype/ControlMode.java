package com.example.robotcontrolsystembackend.domain.enumtype;

/**
 * Control mode for robot arm
 * - CAMERA: Control via AI Camera hand gesture recognition
 * - BUTTON: Control via manual button clicks on frontend
 */
public enum ControlMode {
    CAMERA,
    BUTTON
}
