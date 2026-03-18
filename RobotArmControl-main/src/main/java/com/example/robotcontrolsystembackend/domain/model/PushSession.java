package com.example.robotcontrolsystembackend.domain.model;

import com.example.robotcontrolsystembackend.domain.enumtype.PushProcessingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "push_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "push_session_id")
    private UUID pushSessionId;

    @Column(name = "hub_id", nullable = false)
    private Long hubId;

    @Column(name = "received_time", nullable = false)
    private LocalDateTime receivedTime;

    @Column(name = "total_logs", nullable = false)
    private Integer totalLogs;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, columnDefinition = "push_processing_status_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PushProcessingStatus processingStatus;
}