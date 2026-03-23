package com.example.robotcontrolsystembackend.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RobotControlGrpcServerLifecycle {

    private final RobotControlBridgeGrpcService bridgeService;

    @Value("${robot.grpc.port:50061}")
    private int grpcPort;

    private Server grpcServer;

    @PostConstruct
    public void start() throws IOException {
        grpcServer = NettyServerBuilder.forPort(grpcPort)
                .addService(bridgeService)
                .build()
                .start();

        log.info("Robot control gRPC server started on port {}", grpcPort);
    }

    @PreDestroy
    public void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            log.info("Robot control gRPC server stopped");
        }
    }
}
