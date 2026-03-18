package com.example.robotcontrolsystembackend.application.service.telemetry;

import com.example.robotcontrolsystembackend.domain.model.telemetry.RobotTelemetry;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.cassandra.RobotTelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final RobotTelemetryRepository telemetryRepository;

    public RobotTelemetry saveTelemetry(RobotTelemetry telemetry) {
        if (telemetry.getTelemetryId() == null) {
            telemetry.setTelemetryId(UUID.randomUUID());
        }
        if (telemetry.getTimestamp() == null) {
            telemetry.setTimestamp(Instant.now());
        }
        return telemetryRepository.save(telemetry);
    }

    public List<RobotTelemetry> getTelemetryHistory(Long deviceId, Instant from, Instant to) {
        return telemetryRepository.findByDeviceIdAndTimestampBetween(deviceId, from, to);
    }

    public List<RobotTelemetry> getLatestTelemetry(Long deviceId, int count) {
        return telemetryRepository.findLatestByDeviceId(deviceId, count);
    }
}


