using System;
using System.Collections.Concurrent;
using System.Threading.Tasks;
using Grpc.Core;
using UnityEngine;
using robot;

// Attach this script to a Unity GameObject and assign the 6 joint Transforms in the Inspector.
// Requires the generated C# proto files (robot_control.cs / robot_control.grpc.cs) to be imported into the Unity project,
// and a gRPC C# runtime available for Unity (e.g., via MagicOnion or prebuilt gRPC C# .dlls).
public class RobotControlGrpcServer : MonoBehaviour
{
    public Transform Joint0;
    public Transform Joint1;
    public Transform Joint2;
    public Transform Joint3;
    public Transform Joint4;
    public Transform Joint5;

    public int Port = 50051;

    private Server _server;
    private readonly ConcurrentQueue<Action> _mainThreadQueue = new ConcurrentQueue<Action>();

    void Start()
    {
        StartServer();
    }

    void OnDestroy()
    {
        StopServer();
    }

    void Update()
    {
        while (_mainThreadQueue.TryDequeue(out var act))
        {
            try { act(); } catch (Exception ex) { Debug.LogError("Queued action error: " + ex); }
        }
    }

    public void EnqueueAction(Action a)
    {
        _mainThreadQueue.Enqueue(a);
    }

    public void StartServer()
    {
        try
        {
            _server = new Server
            {
                Services = { RobotControl.BindService(new RobotControlImpl(this)) },
                Ports = { new ServerPort("0.0.0.0", Port, ServerCredentials.Insecure) }
            };
            _server.Start();
            Debug.Log($"gRPC server started on port {Port}");
        }
        catch (Exception ex)
        {
            Debug.LogError("Failed to start gRPC server: " + ex);
        }
    }

    public void StopServer()
    {
        if (_server != null)
        {
            try
            {
                _server.ShutdownAsync().Wait();
            }
            catch (Exception ex)
            {
                Debug.LogError("Error shutting down gRPC server: " + ex);
            }
            _server = null;
        }
    }

    // Apply joint angles on the main Unity thread
    public void ApplyJointAngles(float j0, float j1, float j2, float j3, float j4, float j5)
    {
        // This example assumes each Transform's localRotation uses Euler angles in degrees
        // Adjust mapping as appropriate for your robot rig
        if (Joint0) Joint0.localRotation = Quaternion.Euler(0f, j0, 0f);
        if (Joint1) Joint1.localRotation = Quaternion.Euler(j1, 0f, 0f);
        if (Joint2) Joint2.localRotation = Quaternion.Euler(j2, 0f, 0f);
        if (Joint3) Joint3.localRotation = Quaternion.Euler(0f, j3, 0f);
        if (Joint4) Joint4.localRotation = Quaternion.Euler(j4, 0f, 0f);
        if (Joint5) Joint5.localRotation = Quaternion.Euler(0f, 0f, j5);
    }

    // Implementation of the generated gRPC service base
    private class RobotControlImpl : RobotControl.RobotControlBase
    {
        private readonly RobotControlGrpcServer _owner;

        public RobotControlImpl(RobotControlGrpcServer owner)
        {
            _owner = owner;
        }

        public override Task<ControlResponse> SendRobotCommand(RobotTarget request, ServerCallContext context)
        {
            // Read values from request (properties generated with PascalCase)
            float j0 = request.Joint1;
            float j1 = request.Joint2;
            float j2 = request.Joint3;
            float j3 = request.Joint4;
            float j4 = request.Joint5;
            float j5 = request.Joint6;

            // Enqueue the action so it runs on Unity main thread in Update()
            _owner.EnqueueAction(() => _owner.ApplyJointAngles(j0, j1, j2, j3, j4, j5));

            var resp = new ControlResponse { Success = true, Message = "Angles queued" };
            return Task.FromResult(resp);
        }

        public override async Task<LogSummary> StreamLogs(global::Grpc.Core.IAsyncStreamReader<LogEntry> requestStream, ServerCallContext context)
        {
            int count = 0;
            try
            {
                while (await requestStream.MoveNext())
                {
                    var request = requestStream.Current;
                    Debug.Log($"[RemoteLog {request.Level}] {request.Timestamp}: {request.Message}");
                    count++;
                }
            }
            catch (Exception ex)
            {
                Debug.LogError("Error reading log stream: " + ex);
            }
            var resp = new LogSummary { TotalLogsSent = count };
            return resp;
        }
    }
}
