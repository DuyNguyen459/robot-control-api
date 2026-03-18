package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByHubId(Long hubId);
}
