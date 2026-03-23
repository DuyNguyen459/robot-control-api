package com.example.robotcontrolsystembackend.infrastructure.redis;

import com.example.robotcontrolsystembackend.grpc.RobotControlCommand;
import com.example.robotcontrolsystembackend.infrastructure.grpc.RobotControlStreamHub;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RobotControlRedisBridge implements MessageListener {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RobotControlStreamHub streamHub;

    @Value("${robot.control.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${robot.control.redis.channel:robot.control.commands}")
    private String redisChannel;

    @Value("${robot.control.redis.role:both}")
    private String redisRole;

    private final String instanceId = UUID.randomUUID().toString();

    public boolean publish(RobotControlCommand command) {
        if (!redisEnabled || !canPublish()) {
            return false;
        }

        try {
            RedisControlEnvelope envelope = new RedisControlEnvelope();
            envelope.setInstanceId(instanceId);
            envelope.setRobotId(command.getRobotId());
            envelope.setTimestamp(command.getTimestamp());
            envelope.setSource(command.getSource());
            envelope.setJointAngles(new ArrayList<>(command.getJointAnglesList()));
            envelope.setHasGripper(command.getHasGripper());
            envelope.setGripper(command.getHasGripper() ? command.getGripper() : null);

            String payload = objectMapper.writeValueAsString(envelope);
            redisTemplate.convertAndSend(redisChannel, payload);
            return true;
        } catch (Exception ex) {
            log.warn("Redis publish failed: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isSubscriptionEnabled() {
        return redisEnabled && canSubscribe();
    }

    public String getRedisChannel() {
        return redisChannel;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (!redisEnabled || !canSubscribe()) {
            return;
        }

        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            RedisControlEnvelope envelope = objectMapper.readValue(payload, RedisControlEnvelope.class);

            if (envelope == null || envelope.getRobotId() == null) {
                return;
            }

            if (instanceId.equals(envelope.getInstanceId())) {
                return;
            }

            if (envelope.getJointAngles() == null || envelope.getJointAngles().size() != 6) {
                log.warn("Redis consume ignored invalid jointAngles payload");
                return;
            }

            RobotControlCommand.Builder builder = RobotControlCommand.newBuilder()
                    .setRobotId(envelope.getRobotId())
                    .setTimestamp(envelope.getTimestamp() == null ? "" : envelope.getTimestamp())
                    .setSource(envelope.getSource() == null ? "redis" : envelope.getSource());

            for (Double value : envelope.getJointAngles()) {
                builder.addJointAngles(value == null ? 0.0 : value);
            }

            if (Boolean.TRUE.equals(envelope.getHasGripper()) && envelope.getGripper() != null) {
                builder.setHasGripper(true);
                builder.setGripper(envelope.getGripper());
            }

            int delivered = streamHub.publish(builder.build());
            log.debug("Redis consume delivered to gRPC subscribers={}", delivered);
        } catch (Exception ex) {
            log.warn("Redis consume failed: {}", ex.getMessage());
        }
    }

    private boolean canPublish() {
        String mode = normalizedRole();
        return "api".equals(mode) || "both".equals(mode);
    }

    private boolean canSubscribe() {
        String mode = normalizedRole();
        return "grpc".equals(mode) || "both".equals(mode);
    }

    private String normalizedRole() {
        return redisRole == null ? "both" : redisRole.trim().toLowerCase(Locale.ROOT);
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class RedisControlEnvelope {
        private String instanceId;
        private Long robotId;
        private String timestamp;
        private String source;
        private List<Double> jointAngles;
        private Boolean hasGripper;
        private Integer gripper;
    }
}
