import os
import grpc

try:
    import robot_control_pb2
    import robot_control_pb2_grpc
except Exception as e:
    robot_control_pb2 = None
    robot_control_pb2_grpc = None
import queue
import threading
import time



class RobotGrpcClient:
    def __init__(self, target="localhost:50051"):
        self.target = target
        self.channel = grpc.insecure_channel(self.target)
        if robot_control_pb2_grpc is None:
            raise ImportError("Generated gRPC modules not found. Run: python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. robot_control.proto")
        self.stub = robot_control_pb2_grpc.RobotControlStub(self.channel)

    def send_robot_command(self, angles, timeout=2.0):
        if robot_control_pb2 is None:
            raise ImportError("Generated protobuf module robot_control_pb2 not found.")
        a = angles + [0.0] * max(0, 6 - len(angles))
        req = robot_control_pb2.RobotTarget(
            joint1=float(a[0]),
            joint2=float(a[1]),
            joint3=float(a[2]),
            joint4=float(a[3]),
            joint5=float(a[4]),
            joint6=float(a[5])
        )
        resp = self.stub.SendRobotCommand(req, timeout=timeout)
        return resp


_client = None

def get_client():
    global _client
    if _client is None:
        target = os.getenv("ROBOT_GRPC_TARGET", "localhost:50051")
        _client = RobotGrpcClient(target=target)
    return _client


def send_robot_command(angles, timeout=2.0):
    c = get_client()
    return c.send_robot_command(angles, timeout=timeout)


class _LogStreamer:
    def __init__(self, client: RobotGrpcClient):
        self.client = client
        self._q = queue.Queue()
        self._stop = threading.Event()
        self._thread = None
        self.response = None

    def generator(self):
        # yield LogEntry messages from queue until stop
        while not self._stop.is_set() or not self._q.empty():
            try:
                item = self._q.get(timeout=0.25)
            except queue.Empty:
                continue
            yield item

    def start(self):
        if self._thread is not None:
            return
        def run():
            try:
                # stub.StreamLogs expects an iterable of LogEntry
                self.response = self.client.stub.StreamLogs(self.generator())
            except Exception as e:
                self.response = e
        self._thread = threading.Thread(target=run, daemon=True)
        self._thread.start()

    def stop(self, timeout=5.0):
        self._stop.set()
        if self._thread:
            self._thread.join(timeout)
        return self.response

    def send(self, timestamp, level, message):
        if robot_control_pb2 is None:
            raise ImportError("Generated protobuf module robot_control_pb2 not found.")
        entry = robot_control_pb2.LogEntry(timestamp=str(timestamp), level=str(level), message=str(message))
        self._q.put(entry)


_log_streamer = None

def start_log_stream():
    global _log_streamer
    c = get_client()
    if _log_streamer is None:
        _log_streamer = _LogStreamer(c)
        _log_streamer.start()
    return _log_streamer

def send_log(timestamp, level, message):
    if _log_streamer is None:
        start_log_stream()
    try:
        _log_streamer.send(timestamp, level, message)
    except Exception as e:
        print(f"Failed to enqueue log: {e}")

def stop_log_stream():
    global _log_streamer
    if _log_streamer is None:
        return None
    resp = _log_streamer.stop()
    _log_streamer = None
    return resp

