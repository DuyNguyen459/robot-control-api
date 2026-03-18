package com.example.robotcontrolsystembackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "robot_joint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotJoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "joint_id")
    private Long jointId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "joint_index", nullable = false)
    private Integer jointIndex;

    @Column(name = "joint_name", nullable = false, length = 10)
    private String jointName;

    @Column(name = "min_angle", nullable = false)
    private Double minAngle;

    @Column(name = "max_angle", nullable = false)
    private Double maxAngle;

    @Column(name = "max_speed")
    private Double maxSpeed;
}