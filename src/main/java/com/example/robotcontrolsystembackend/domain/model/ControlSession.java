package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "mode", nullable = false, length = 20)
    private String mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, columnDefinition = "session_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SessionStatus sessionStatus;
}