package com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra;

import com.example.robotcontrolsystembackend.domain.model.logging.RobotLatestStatus;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RobotLatestStatusRepository extends CassandraRepository<RobotLatestStatus, Long> {
}
