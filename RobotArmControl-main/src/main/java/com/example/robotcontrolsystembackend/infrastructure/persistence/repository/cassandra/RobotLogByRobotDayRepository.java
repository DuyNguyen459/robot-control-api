package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;

import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDay;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RobotLogByRobotDayRepository extends CassandraRepository<RobotLogByRobotDay, RobotLogByRobotDayKey> {

    @Query("SELECT * FROM robot_logs_by_robot_day WHERE robot_id = ?0 AND log_date = ?1 LIMIT ?2")
    List<RobotLogByRobotDay> findLatestByRobotAndDate(Long robotId, LocalDate logDate, int limit);
}
