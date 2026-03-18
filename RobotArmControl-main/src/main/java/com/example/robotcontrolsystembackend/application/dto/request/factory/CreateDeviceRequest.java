package com.example.robotcontrolsystembackend.application.dto.request.factory;

import com.example.robotcontrolsystembackend.domain.enumtype.ConnectionType;
import com.example.robotcontrolsystembackend.domain.enumtype.DeviceType;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateDeviceRequest {
    private String deviceName;
    private DeviceType deviceType;
    private RobotType robotType;
    private String model;
    private String serialNumber;
    private ConnectionType connectionType;
}
