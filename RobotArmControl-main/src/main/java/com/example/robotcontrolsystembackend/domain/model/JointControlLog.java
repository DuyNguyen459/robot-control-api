package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.JointStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "joint_control_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JointControlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "joint_log_id")
    private Long jointLogId;

    @Column(name = "log_id", nullable = false)
    private Long logId;

    @Column(name = "joint_id", nullable = false)
    private Long jointId;

    @Column(name = "target_angle", nullable = false)
    private Double targetAngle;

    @Column(name = "actual_angle")
    private Double actualAngle;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "torque")
    private Double torque;

    @Enumerated(EnumType.STRING)
    @Column(name = "joint_status", nullable = false, columnDefinition = "joint_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private JointStatus jointStatus;
}