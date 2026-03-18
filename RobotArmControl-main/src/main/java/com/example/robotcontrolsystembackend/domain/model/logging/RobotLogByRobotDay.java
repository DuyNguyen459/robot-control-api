package com.example.robotcontrolsystembackend.domain.model.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("robot_logs_by_robot_day")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotLogByRobotDay {

    @PrimaryKey
    private RobotLogByRobotDayKey key;

    @Column("session_id")
    private Long sessionId;

    @Column("user_id")
    private Long userId;

    @Column("factory_id")
    private Long factoryId;

    @Column("log_type")
    private String logType;

    @Column("severity")
    private String severity;

    @Column("status")
    private String status;

    @Column("command")
    private String command;

    @Column("source")
    private String source;

    @Column("message")
    private String message;

    @Column("trace_id")
    private String traceId;

    @Column("metadata_json")
    private String metadataJson;
}
