package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;

import com.example.robotcontrolsystembackend.domain.model.logging.RobotAlertByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotAlertByRobotDayKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RobotAlertByRobotDayRepository extends CassandraRepository<RobotAlertByRobotDay, RobotAlertByRobotDayKey> {

    @Query("SELECT * FROM robot_alerts_by_robot_day WHERE robot_id = ?0 AND log_date = ?1 LIMIT ?2")
    List<RobotAlertByRobotDay> findAlertsByRobotAndDate(Long robotId, LocalDate logDate, int limit);

    @Query("SELECT * FROM robot_alerts_by_robot_day WHERE robot_id = ?0 AND log_date = ?1 AND severity = ?2 LIMIT ?3")
    List<RobotAlertByRobotDay> findAlertsByRobotAndDateAndSeverity(Long robotId, LocalDate logDate, String severity, int limit);
}
