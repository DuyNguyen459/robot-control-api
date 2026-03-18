package com.example.robotcontrolsystembackend.infrastructure.persistence.repository;

import com.example.robotcontrolsystembackend.domain.enumtype.DeviceStatus;
import com.example.robotcontrolsystembackend.domain.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    boolean existsByHubId(Long hubId);
    boolean existsBySerialNumber(String serialNumber);
    List<Device> findByHubIdOrderByDeviceIdAsc(Long hubId);
    List<Device> findByHubIdAndDeviceStatusOrderByDeviceIdAsc(Long hubId, DeviceStatus deviceStatus);
}
