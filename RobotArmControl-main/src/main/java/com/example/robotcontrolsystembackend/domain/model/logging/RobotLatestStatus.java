package com.example.robotcontrolsystembackend.domain.model.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("robot_latest_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotLatestStatus {

    @PrimaryKey("robot_id")
    private Long robotId;

    @Column("last_event_time")
    private Instant lastEventTime;

    @Column("session_id")
    private Long sessionId;

    @Column("user_id")
    private Long userId;

    @Column("factory_id")
    private Long factoryId;

    @Column("status")
    private String status;

    @Column("severity")
    private String severity;

    @Column("source")
    private String source;

    @Column("message")
    private String message;

    @Column("trace_id")
    private String traceId;

    @Column("metadata_json")
    private String metadataJson;
}
