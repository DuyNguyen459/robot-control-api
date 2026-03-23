package com.example.robotcontrolsystembackend.infrastructure.grpc;

import com.example.robotcontrolsystembackend.grpc.RobotControlBridgeGrpc;
import com.example.robotcontrolsystembackend.grpc.RobotControlCommand;
import com.example.robotcontrolsystembackend.grpc.SubscribeControlRequest;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RobotControlBridgeGrpcService extends RobotControlBridgeGrpc.RobotControlBridgeImplBase {

    private final RobotControlStreamHub streamHub;

    @Override
    public void subscribeControl(SubscribeControlRequest request, StreamObserver<RobotControlCommand> responseObserver) {
        long robotId = request.getRobotId();
        String clientId = request.getClientId();

        long subscriberId = streamHub.register(robotId, clientId, responseObserver);

        if (responseObserver instanceof ServerCallStreamObserver<RobotControlCommand> serverObserver) {
            serverObserver.setOnCancelHandler(() -> streamHub.unregister(subscriberId));
        }

        log.info("Unity subscribed to control stream: subscriberId={} robotId={} clientId={}",
                subscriberId, robotId, clientId);
    }
}
