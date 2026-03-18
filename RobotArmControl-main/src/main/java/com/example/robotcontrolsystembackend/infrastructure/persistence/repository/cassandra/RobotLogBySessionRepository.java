package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;

import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogBySession;
import com.example.robotcontrolsystembackend.domain.model.logging.RobotLogBySessionKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotLogBySessionRepository extends CassandraRepository<RobotLogBySession, RobotLogBySessionKey> {

    @Query("SELECT * FROM robot_logs_by_session WHERE session_id = ?0 LIMIT ?1")
    List<RobotLogBySession> findBySessionId(Long sessionId, int limit);
}
