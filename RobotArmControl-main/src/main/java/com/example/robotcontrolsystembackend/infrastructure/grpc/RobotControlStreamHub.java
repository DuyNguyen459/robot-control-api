package com.example.robotcontrolsystembackend.infrastructure.grpc;

import com.example.robotcontrolsystembackend.grpc.RobotControlCommand;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RobotControlStreamHub {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final Map<Long, Subscriber> subscribers = new ConcurrentHashMap<>();

    public long register(long robotId, String clientId, StreamObserver<RobotControlCommand> observer) {
        long subscriberId = idSequence.incrementAndGet();
        subscribers.put(subscriberId, new Subscriber(subscriberId, robotId, clientId, observer));
        log.info("gRPC subscribe: subscriberId={} robotId={} clientId={} totalSubscribers={}",
                subscriberId, robotId, clientId, subscribers.size());
        return subscriberId;
    }

    public void unregister(long subscriberId) {
        Subscriber removed = subscribers.remove(subscriberId);
        if (removed != null) {
            log.info("gRPC unsubscribe: subscriberId={} robotId={} clientId={} totalSubscribers={}",
                    removed.getSubscriberId(), removed.getRobotId(), removed.getClientId(), subscribers.size());
        }
    }

    public int publish(RobotControlCommand command) {
        int delivered = 0;
        for (Subscriber subscriber : subscribers.values()) {
            if (!matches(subscriber.getRobotId(), command.getRobotId())) {
                continue;
            }
            if (subscriber.send(command)) {
                delivered += 1;
            } else {
                unregister(subscriber.getSubscriberId());
            }
        }

        log.debug("gRPC publish command robotId={} delivered={}", command.getRobotId(), delivered);
        return delivered;
    }

    private boolean matches(long subscriberRobotId, long commandRobotId) {
        // robotId=0 means receive all robots.
        return subscriberRobotId == 0 || subscriberRobotId == commandRobotId;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Subscriber {
        private final long subscriberId;
        private final long robotId;
        private final String clientId;
        private final StreamObserver<RobotControlCommand> observer;
        private final Object writeLock = new Object();

        boolean send(RobotControlCommand command) {
            try {
                synchronized (writeLock) {
                    observer.onNext(command);
                }
                return true;
            } catch (Exception ex) {
                log.warn("gRPC push failed for subscriberId={}: {}", subscriberId, ex.getMessage());
                try {
                    observer.onError(ex);
                } catch (Exception ignored) {
                    // no-op
                }
                return false;
            }
        }
    }
}
