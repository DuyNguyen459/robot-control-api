"""
Simple Python gRPC client that streams Landmark messages to a gRPC server.
Generate Python protobufs first:

  python -m pip install grpcio grpcio-tools
  python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. src/main/proto/robot_arm.proto

Copy the generated `robot_arm_pb2.py` and `robot_arm_pb2_grpc.py` into this folder or ensure PYTHONPATH includes the repo root.
"""
import time
import grpc

try:
    import robot_arm_pb2
    import robot_arm_pb2_grpc
except Exception:
    robot_arm_pb2 = None
    robot_arm_pb2_grpc = None


class RobotArmStreamer:
    def __init__(self, target="localhost:50051"):
        self.target = target
        self.channel = grpc.insecure_channel(self.target)
        if robot_arm_pb2_grpc is None:
            raise ImportError(
                "Generated gRPC modules not found. Run: python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. src/main/proto/robot_arm.proto"
            )
        self.stub = robot_arm_pb2_grpc.RobotArmStub(self.channel)

    def stream_landmarks(self, landmarks_iter, timeout=None):
        """Send a client-streaming RPC. `landmarks_iter` yields dicts or objects with x,y,z,id,timestamp."""

        def gen():
            for lm in landmarks_iter:
                msg = robot_arm_pb2.Landmark(
                    id=int(lm.get("id", 0)) if isinstance(lm, dict) else int(getattr(lm, "id", 0)),
                    x=float(lm["x"]) if isinstance(lm, dict) else float(getattr(lm, "x", 0.0)),
                    y=float(lm["y"]) if isinstance(lm, dict) else float(getattr(lm, "y", 0.0)),
                    z=float(lm.get("z", 0.0)) if isinstance(lm, dict) else float(getattr(lm, "z", 0.0)),
                    timestamp=int(lm.get("timestamp", int(time.time() * 1000))) if isinstance(lm, dict) else int(getattr(lm, "timestamp", int(time.time() * 1000)))
                )
                yield msg

        return self.stub.StreamLandmarks(gen(), timeout=timeout)


if __name__ == "__main__":
    # Example: stream 100 synthetic landmarks at 30 Hz
    streamer = RobotArmStreamer("localhost:50051")

    def synth():
        for i in range(100):
            yield {"id": i, "x": 0.5 + 0.01 * i, "y": 0.4, "z": 0.0, "timestamp": int(time.time() * 1000)}
            time.sleep(1.0 / 30.0)

    resp = streamer.stream_landmarks(synth(), timeout=20.0)
    print("Server ack:", resp)
