package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;

import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayType;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogByRobotDayTypeKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RobotLogByRobotDayTypeRepository extends CassandraRepository<RobotLogByRobotDayType, RobotLogByRobotDayTypeKey> {

    @Query("SELECT * FROM robot_logs_by_robot_day_type WHERE robot_id = ?0 AND log_date = ?1 AND log_type = ?2 LIMIT ?3")
    List<RobotLogByRobotDayType> findByRobotAndDateAndType(Long robotId, LocalDate logDate, String logType, int limit);
}
