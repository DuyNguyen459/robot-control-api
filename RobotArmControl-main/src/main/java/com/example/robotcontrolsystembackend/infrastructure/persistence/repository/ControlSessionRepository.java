package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;
import com.example.robotcontrolsystembackend.domain.model.ControlSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlSessionRepository extends JpaRepository<ControlSession, Long> {

	Optional<ControlSession> findFirstBySessionStatusOrderByStartTimeDesc(SessionStatus sessionStatus);

	Page<ControlSession> findBySessionStatus(SessionStatus sessionStatus, Pageable pageable);
}
