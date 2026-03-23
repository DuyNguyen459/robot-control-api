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
import java.net.InetSocketAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class RobotControlGrpcServerLifecycle {

    private final RobotControlBridgeGrpcService bridgeService;

    @Value("${robot.grpc.port:50061}")
    private int grpcPort;

    @Value("${robot.grpc.bind-address:0.0.0.0}")
    private String grpcBindAddress;

    @Value("${robot.grpc.use-port-env:false}")
    private boolean usePortEnv;

    private Server grpcServer;

    @PostConstruct
    public void start() throws IOException {
        int resolvedPort = resolveGrpcPort();
        grpcServer = NettyServerBuilder.forAddress(new InetSocketAddress(grpcBindAddress, resolvedPort))
                .addService(bridgeService)
                .build()
                .start();

        log.info("Robot control gRPC server started on {}:{} (usePortEnv={})", grpcBindAddress, resolvedPort, usePortEnv);
    }

    private int resolveGrpcPort() {
        if (!usePortEnv) {
            return grpcPort;
        }

        String portFromEnv = System.getenv("PORT");
        if (portFromEnv == null || portFromEnv.isBlank()) {
            log.warn("robot.grpc.use-port-env=true but PORT is missing. Falling back to robot.grpc.port={}", grpcPort);
            return grpcPort;
        }

        try {
            return Integer.parseInt(portFromEnv);
        } catch (NumberFormatException ex) {
            log.warn("Invalid PORT='{}'. Falling back to robot.grpc.port={}", portFromEnv, grpcPort);
            return grpcPort;
        }
    }

    @PreDestroy
    public void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            log.info("Robot control gRPC server stopped");
        }
    }
}
