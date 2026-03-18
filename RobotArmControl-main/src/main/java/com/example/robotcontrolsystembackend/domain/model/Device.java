package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.ConnectionType;
import com.example.robotcontrolsystembackend.domain.enumtype.DeviceStatus;
import com.example.robotcontrolsystembackend.domain.enumtype.DeviceType;
import com.example.robotcontrolsystembackend.domain.enumtype.RobotType;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "hub_id", nullable = false)
    private Long hubId;

    @Column(name = "device_name", nullable = false, length = 150)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, columnDefinition = "device_type_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "robot_type", columnDefinition = "robot_type_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RobotType robotType;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, columnDefinition = "connection_type_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ConnectionType connectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, columnDefinition = "device_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private DeviceStatus deviceStatus;
}