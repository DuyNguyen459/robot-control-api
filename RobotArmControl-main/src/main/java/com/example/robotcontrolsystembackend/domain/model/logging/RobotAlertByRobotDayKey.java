package com.example.robotcontrolsystembackend.domain.model.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PrimaryKeyClass
public class RobotAlertByRobotDayKey {

    @PrimaryKeyColumn(name = "robot_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long robotId;

    @PrimaryKeyColumn(name = "log_date", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private LocalDate logDate;

    @PrimaryKeyColumn(name = "severity", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
    private String severity;

    @PrimaryKeyColumn(name = "event_time", ordinal = 3, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant eventTime;

    @PrimaryKeyColumn(name = "event_id", ordinal = 4, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private UUID eventId;
}
