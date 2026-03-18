package com.example.robotcontrolsystembackend.domain.model.telemetry;


import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;


import java.time.Instant;
import java.util.UUID;


@Table("robot_telemetry")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RobotTelemetry {

    @PrimaryKeyColumn(name = "device_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long deviceId;

    @PrimaryKeyColumn(name = "timestamp", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private Instant timestamp;

    @Column("telemetry_id")
    private UUID telemetryId;

    @Column("session_id")
    private Long sessionId;

    @Column("user_id")
    private Long userId;

    @Column("joint_angles")
    private String jointAngles;         // JSON array

    @Column("target_angles")
    private String targetAngles;

    @Column("joint_speeds")
    private String jointSpeeds;

    @Column("gripper_status")
    private String gripperStatus;

    @Column("gesture_detected")
    private String gestureDetected;

    @Column("gesture_confidence")
    private Double gestureConfidence;

    @Column("fingers_count")
    private Integer fingersCount;

    @Column("selected_joint")
    private Integer selectedJoint;

    @Column("control_signal")
    private Double controlSignal;

    @Column("battery_level")
    private Double batteryLevel;

    @Column("temperature")
    private Double temperature;

    @Column("connection_latency_ms")
    private Integer connectionLatencyMs;

    @Column("source")
    private String source;

    @Column("error_codes")
    private String errorCodes;

    @Column("raw_data")
    private String rawData;
}



