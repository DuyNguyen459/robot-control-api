package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;


import com.example.robotcontrolsystembackend.domain.model.telemetry.RobotTelemetry;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;


@Repository
public interface RobotTelemetryRepository extends CassandraRepository<RobotTelemetry, Long> {


    // Lấy telemetry trong khoảng thời gian
    @Query("SELECT * FROM robot_telemetry WHERE device_id = ?0 AND timestamp >= ?1 AND timestamp <= ?2")
    List<RobotTelemetry> findByDeviceIdAndTimestampBetween(Long deviceId, Instant start, Instant end);


    // Lấy telemetry mới nhất của device
    @Query("SELECT * FROM robot_telemetry WHERE device_id = ?0 ORDER BY timestamp DESC LIMIT ?1")
    List<RobotTelemetry> findLatestByDeviceId(Long deviceId, int limit);
}


