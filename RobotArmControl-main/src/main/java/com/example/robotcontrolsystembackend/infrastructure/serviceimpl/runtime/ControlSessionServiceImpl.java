package com.example.robotcontrolsystembackend.infrastructure.serviceimpl.runtime;

import com.example.robotcontrolsystembackend.application.dto.request.runtime.StartSessionRequest;
import com.example.robotcontrolsystembackend.application.dto.response.runtime.SessionStatusResponse;
import com.example.robotcontrolsystembackend.application.service.runtime.ControlSessionService;
import com.example.robotcontrolsystembackend.common.response.PageResponse;
import com.example.robotcontrolsystembackend.config.websocket.RobotControlWebSocketHandler;
import com.example.robotcontrolsystembackend.domain.enumtype.ControlMode;
import com.example.robotcontrolsystembackend.domain.enumtype.SessionStatus;
import com.example.robotcontrolsystembackend.domain.model.ControlSession;
import com.example.robotcontrolsystembackend.domain.model.User;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.ControlSessionRepository;
import com.example.robotcontrolsystembackend.infrastructure.persistence.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControlSessionServiceImpl implements ControlSessionService {
    
    private final RobotControlWebSocketHandler webSocketHandler;
    private final ControlSessionRepository controlSessionRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile boolean cameraActive = false;
    
    @Override
    @Transactional
    public SessionStatusResponse startSession(StartSessionRequest request) {
        ControlMode mode = request.getControlMode();

        if (mode == null) {
            throw new IllegalArgumentException("controlMode is required");
        }

        // Only one active session at a time: close previous running session.
        Optional<ControlSession> activeOpt = findActiveSession();
        if (activeOpt.isPresent()) {
            closeSession(activeOpt.get());
        }

        User currentUser = getCurrentUser();
        Long resolvedDeviceId = resolveDeviceId(request.getDeviceId());

        ControlSession created = controlSessionRepository.save(ControlSession.builder()
                .userId(currentUser.getUserId())
                .factoryId(currentUser.getFactory() != null ? currentUser.getFactory().getFactoryId() : null)
                .deviceId(resolvedDeviceId)
                .startTime(LocalDateTime.now())
                .mode(mode.name())
                .sessionStatus(SessionStatus.Running)
                .build());

        String message;

        if (mode == ControlMode.CAMERA) {
            // Start AI Camera mode - broadcast START command
            String wsMessage = "{\"type\":\"camera_control\",\"command\":\"START\"}";
            webSocketHandler.broadcastMessage(wsMessage);
            cameraActive = true;
            message = "Session started with CAMERA mode - AI Camera activated";
            log.info("Control session started - sessionId={} CAMERA mode, START_CAMERA command sent", created.getSessionId());
        } else {
            // BUTTON mode - no camera activation
            cameraActive = false;
            message = "Session started with BUTTON mode - Manual control enabled";
            log.info("Control session started - sessionId={} BUTTON mode", created.getSessionId());
        }

        return toSessionStatus(created, true, message);
    }
    
    @Override
    @Transactional
    public SessionStatusResponse stopSession() {
        Optional<ControlSession> activeOpt = findActiveSession();
        if (activeOpt.isEmpty()) {
            cameraActive = false;
            return SessionStatusResponse.builder()
                    .sessionActive(false)
                    .controlMode(null)
                    .connectedClients(webSocketHandler.getConnectedClientsCount())
                    .cameraActive(false)
                    .message("No active session")
                    .build();
        }

        ControlSession active = activeOpt.get();
        ControlMode previousMode = parseMode(active.getMode());

        if (previousMode == ControlMode.CAMERA || cameraActive) {
            String wsMessage = "{\"type\":\"camera_control\",\"command\":\"STOP\"}";
            webSocketHandler.broadcastMessage(wsMessage);
            log.info("STOP_CAMERA command sent");
        }

        closeSession(active);
        cameraActive = false;

        log.info("Control session stopped (sessionId={}, was {} mode)", active.getSessionId(), previousMode);

        return toSessionStatus(active, false, "Session stopped successfully");
    }
    
    @Override
    public SessionStatusResponse getSessionStatus() {
        Optional<ControlSession> activeOpt = findActiveSession();
        if (activeOpt.isEmpty()) {
            cameraActive = false;
            return SessionStatusResponse.builder()
                .sessionActive(false)
                .controlMode(null)
                .connectedClients(webSocketHandler.getConnectedClientsCount())
                .cameraActive(false)
                .message("No active session")
                .build();
        }

        ControlSession active = activeOpt.get();
        ControlMode mode = parseMode(active.getMode());
        cameraActive = mode == ControlMode.CAMERA;

        return toSessionStatus(active, true, "Session active with " + mode + " mode");
    }

    @Override
    public PageResponse<SessionStatusResponse> getSessions(SessionStatus status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startTime"));

        Page<ControlSession> sourcePage = (status == null)
                ? controlSessionRepository.findAll(pageable)
                : controlSessionRepository.findBySessionStatus(status, pageable);

        List<SessionStatusResponse> content = sourcePage.getContent().stream()
                .map(this::toSessionStatusFromEntity)
                .toList();

        return PageResponse.of(sourcePage, content);
    }

    @Override
    public SessionStatusResponse getSessionById(Long sessionId) {
        ControlSession session = controlSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

        return toSessionStatusFromEntity(session);
    }
    
    @Override
    public boolean isSessionActive() {
        return findActiveSession().isPresent();
    }
    
    @Override
    public ControlMode getCurrentControlMode() {
        return findActiveSession().map(s -> parseMode(s.getMode())).orElse(null);
    }
    
    @Override
    public int getConnectedClientsCount() {
        return webSocketHandler.getConnectedClientsCount();
    }
    
    @Override
    public boolean sendAngleCommand(int jointIndex, double angle) {
        Optional<ControlSession> activeOpt = findActiveSession();
        if (activeOpt.isEmpty() || parseMode(activeOpt.get().getMode()) != ControlMode.BUTTON) {
            log.warn("Cannot send angle command - session not active or not in BUTTON mode");
            return false;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "button_angle");
            payload.put("jointIndex", jointIndex);
            payload.put("angle", angle);
            
            String message = objectMapper.writeValueAsString(payload);
            webSocketHandler.broadcastMessage(message);
            log.debug("Sent angle command: joint={}, angle={}", jointIndex, angle);
            return true;
        } catch (Exception e) {
            log.error("Failed to send angle command: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean sendAllAngles(double[] angles) {
        Optional<ControlSession> activeOpt = findActiveSession();
        if (activeOpt.isEmpty() || parseMode(activeOpt.get().getMode()) != ControlMode.BUTTON) {
            log.warn("Cannot send angles - session not active or not in BUTTON mode");
            return false;
        }
        
        if (angles == null || angles.length != 6) {
            log.warn("Invalid angles array - must have exactly 6 values");
            return false;
        }
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "button_angles");
            payload.put("angles", Arrays.stream(angles).boxed().toArray());
            
            String message = objectMapper.writeValueAsString(payload);
            webSocketHandler.broadcastMessage(message);
            log.debug("Sent all angles: {}", Arrays.toString(angles));
            return true;
        } catch (Exception e) {
            log.error("Failed to send angles: {}", e.getMessage());
            return false;
        }
    }

    private Optional<ControlSession> findActiveSession() {
        return controlSessionRepository.findFirstBySessionStatusOrderByStartTimeDesc(SessionStatus.Running);
    }

    private void closeSession(ControlSession session) {
        session.setSessionStatus(SessionStatus.Ended);
        session.setEndTime(LocalDateTime.now());
        controlSessionRepository.save(session);
    }

    private ControlMode parseMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return null;
        }
        try {
            return ControlMode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalArgumentException("Authenticated user is required to create a session");
        }
        return user;
    }

    private Long resolveDeviceId(Long requestedDeviceId) {
        if (requestedDeviceId != null) {
            if (!deviceRepository.existsById(requestedDeviceId)) {
                throw new IllegalArgumentException("Device not found with id: " + requestedDeviceId);
            }
            return requestedDeviceId;
        }

        return deviceRepository.findAll().stream()
                .findFirst()
                .map(d -> d.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("No device available. Provide deviceId when starting a session"));
    }

    private SessionStatusResponse toSessionStatus(ControlSession session, boolean active, String message) {
        ControlMode mode = parseMode(session.getMode());
        return SessionStatusResponse.builder()
                .sessionActive(active)
                .controlMode(mode)
                .connectedClients(webSocketHandler.getConnectedClientsCount())
                .cameraActive(mode == ControlMode.CAMERA && active)
                .message(message)
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .factoryId(session.getFactoryId())
                .deviceId(session.getDeviceId())
                .sessionStatus(session.getSessionStatus())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }

    private SessionStatusResponse toSessionStatusFromEntity(ControlSession session) {
        boolean active = session.getSessionStatus() == SessionStatus.Running;
        ControlMode mode = parseMode(session.getMode());
        String message = active
                ? "Session active with " + mode + " mode"
                : "Session ended";

        return SessionStatusResponse.builder()
                .sessionActive(active)
                .controlMode(mode)
                .connectedClients(webSocketHandler.getConnectedClientsCount())
                .cameraActive(active && mode == ControlMode.CAMERA)
                .message(message)
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .factoryId(session.getFactoryId())
                .deviceId(session.getDeviceId())
                .sessionStatus(session.getSessionStatus())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .build();
    }
}
