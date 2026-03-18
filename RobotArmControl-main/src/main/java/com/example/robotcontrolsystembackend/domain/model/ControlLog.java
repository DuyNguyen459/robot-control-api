package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @Column(name = "hand_gesture_id", nullable = false)
    private Long handGestureId;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "command_id", nullable = false)
    private Long commandId;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, columnDefinition = "execution_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ExecutionStatus executionStatus;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}