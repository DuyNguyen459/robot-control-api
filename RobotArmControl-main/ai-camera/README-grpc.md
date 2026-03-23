gRPC + Protobuf quick guide for RobotArm streaming

1) Generate Python code

  python -m pip install grpcio grpcio-tools
  python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. src/main/proto/robot_arm.proto

This produces `robot_arm_pb2.py` and `robot_arm_pb2_grpc.py`. Copy them into this folder or ensure PYTHONPATH includes the repo root.

2) Generate C# code (for Unity)

  - Install `protoc` and `grpc_csharp_plugin` for your platform.
  - Generate:

    protoc -I. --csharp_out=./UnityGenerated --grpc_out=./UnityGenerated --plugin=protoc-gen-grpc=path/to/grpc_csharp_plugin.exe src/main/proto/robot_arm.proto

  - Copy generated C# files into `Assets/Plugins/` in your Unity project (or another Plugins folder), keep namespace `RobotArm.Grpc`.
  - Add gRPC C# runtime packages to Unity (e.g., via Unity's package manager or include native gRPC C# binaries). Alternatively run the server as a separate .NET process using `Grpc.Core`.

3) Python usage

  Use `ai-camera/robot_arm_client.py` as an example. The client opens an insecure channel to `localhost:50051` and streams `Landmark` messages.

  In your MediaPipe pipeline, for each detected landmark do something like:

    req = {"id": idx, "x": landmark.x, "y": landmark.y, "z": landmark.z, "timestamp": ts}
    yield req

  The client example streams an iterator of such dicts.

4) Unity/C# usage

  The server example `docs/unity_server/RobotArmGrpcServer.cs` shows how to implement `StreamLandmarks` and read each incoming `Landmark` (mapped to C# object). Inside the loop, assign `lm.X/lm.Y` to your robot control interface.

Notes
- For a true in-Unity server that manipulates GameObjects you must marshal updates to Unity's main thread (e.g., using a ConcurrentQueue and executing in `Update()`).
- If packaging the server inside Unity is difficult, run the C# server as a separate process and communicate internally to Unity via IPC or sockets.
