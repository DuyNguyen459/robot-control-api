package com.example.robotcontrolsystembackend.config.websocket;


import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class RobotControlWebSocketHandler extends TextWebSocketHandler {


    private static final int SEND_TIME_LIMIT_MS = 5000;
    private static final int SEND_BUFFER_SIZE_BYTES = 512 * 1024;

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String ATTR_LAST_AI_ANGLES_LOG_MS = "lastAiAnglesLogMs";
    private static final long AI_ANGLES_LOG_INTERVAL_MS = 1000;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
            session,
            SEND_TIME_LIMIT_MS,
            SEND_BUFFER_SIZE_BYTES
        );
        sessions.put(session.getId(), safeSession);
        Object username = session.getAttributes().get("username");
        Object authenticated = session.getAttributes().get("authenticated");
        log.info(
                "WS client connected: id={} user={} auth={} totalClients={}",
                session.getId(),
                username != null ? username : "?",
                authenticated != null ? authenticated : "?",
                sessions.size()
        );
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        String type = null;
        Integer anglesLen = null;
        String action = null;
        try {
            JsonNode root = OBJECT_MAPPER.readTree(payload);
            JsonNode typeNode = root.get("type");
            if (typeNode != null && typeNode.isTextual()) {
                type = typeNode.asText();
            }
            if ("ai_angles".equals(type)) {
                JsonNode anglesNode = root.get("angles");
                if (anglesNode != null && anglesNode.isArray()) {
                    anglesLen = anglesNode.size();
                }
            } else if ("robot_command".equals(type)) {
                JsonNode actionNode = root.get("action");
                if (actionNode != null && actionNode.isTextual()) {
                    action = actionNode.asText();
                }
            }
        } catch (Exception ignored) {
            // Not JSON; ignore
        }

        if ("ai_angles".equals(type)) {
            long now = System.currentTimeMillis();
            Object lastObj = session.getAttributes().get(ATTR_LAST_AI_ANGLES_LOG_MS);
            long last = (lastObj instanceof Number) ? ((Number) lastObj).longValue() : 0L;
            if (now - last >= AI_ANGLES_LOG_INTERVAL_MS) {
                session.getAttributes().put(ATTR_LAST_AI_ANGLES_LOG_MS, now);
                log.info(
                        "WS recv ai_angles: fromId={} clients={} anglesLen={}",
                        session.getId(),
                        sessions.size(),
                        anglesLen
                );
            }
        } else if ("robot_command".equals(type)) {
            log.info("WS recv robot_command: fromId={} clients={} action={}", session.getId(), sessions.size(), action);
        } else {
            log.debug("WS recv: fromId={} payload={}", session.getId(), payload);
        }

        // Broadcast to all clients (Unity, Frontend, etc.)
        broadcastRawPayload(payload);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("Client disconnected: {} | Remaining: {}", session.getId(), sessions.size());
    }


    /**
     * Broadcast a message to all connected clients
     * Used by ControlSessionService to send camera control commands
     */
    public void broadcastMessage(String message) {
        broadcastRawPayload(message);
        log.info("Broadcast message to {} clients: {}", sessions.size(), message);
    }

    public boolean sendToDevice(String deviceId, String message) {
        // Current WS topology does not bind a specific session to a device.
        // Payload contains deviceId and Unity clients filter by deviceId.
        int delivered = broadcastRawPayload(message);
        return delivered > 0;
    }


    /**
     * Get the number of connected clients
     */
    public int getConnectedClientsCount() {
        return sessions.size();
    }

    private int broadcastRawPayload(String payload) {
        int delivered = 0;
        for (WebSocketSession s : sessions.values()) {
            if (sendSafely(s, payload)) {
                delivered += 1;
            }
        }
        return delivered;
    }

    private boolean sendSafely(WebSocketSession session, String payload) {
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            session.sendMessage(new TextMessage(payload));
            return true;
        } catch (IOException | IllegalStateException e) {
            log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
            return false;
        }
    }
}

