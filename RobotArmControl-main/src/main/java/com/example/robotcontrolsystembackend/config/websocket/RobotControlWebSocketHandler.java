package com.example.robotcontrolsystembackend.config.websocket;


import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class RobotControlWebSocketHandler extends TextWebSocketHandler {


    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String ATTR_LAST_AI_ANGLES_LOG_MS = "lastAiAnglesLogMs";
    private static final long AI_ANGLES_LOG_INTERVAL_MS = 1000;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
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
        } else {
            log.debug("WS recv: fromId={} payload={}", session.getId(), payload);
        }

        // Broadcast to all clients (Unity, Frontend, etc.)
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Client disconnected: {} | Remaining: {}", session.getId(), sessions.size());
    }


    /**
     * Broadcast a message to all connected clients
     * Used by ControlSessionService to send camera control commands
     */
    public void broadcastMessage(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Failed to send message to session {}: {}", s.getId(), e.getMessage());
                }
            }
        }
        log.info("Broadcast message to {} clients: {}", sessions.size(), message);
    }


    /**
     * Get the number of connected clients
     */
    public int getConnectedClientsCount() {
        return sessions.size();
    }
}

