using System;
using System.Collections.Concurrent;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Net.Client;
using RobotControl.Grpc;
using UnityEngine;

// Unity-side gRPC client: subscribes to Backend stream and pushes commands to NiryoJointDriver.
// This avoids hosting Grpc.Core.Server inside Unity and therefore avoids grpc_csharp_ext native-plugin issues.
public class RobotArmGrpcClientBehaviour : MonoBehaviour
{
    [Header("Assign robot driver")]
    public NiryoJointDriver jointDriver;

    [Header("Backend gRPC endpoint")]
    public string BackendAddress = "http://127.0.0.1:50061";

    [Header("Subscription")]
    public long RobotId = 1;
    public string ClientId = "unity-client";

    [Header("Optional gripper mapping")]
    public bool useJoint5AsGripper = true;
    public float gripperOpenAngle = 0f;
    public float gripperClosedAngle = 45f;

    [Header("Reconnect")]
    public float reconnectDelaySeconds = 2f;

    private readonly ConcurrentQueue<RobotControlCommand> incomingQueue = new ConcurrentQueue<RobotControlCommand>();
    private CancellationTokenSource cts;

    private void Start()
    {
        if (jointDriver == null)
        {
            Debug.LogError("RobotArmGrpcClientBehaviour: jointDriver not assigned");
            enabled = false;
            return;
        }

        cts = new CancellationTokenSource();
        _ = Task.Run(() => SubscribeLoopAsync(cts.Token));
    }

    private void OnDestroy()
    {
        if (cts != null)
        {
            cts.Cancel();
            cts.Dispose();
            cts = null;
        }
    }

    private void Update()
    {
        while (incomingQueue.TryDequeue(out var cmd))
        {
            ApplyCommandOnMainThread(cmd);
        }
    }

    private async Task SubscribeLoopAsync(CancellationToken token)
    {
        while (!token.IsCancellationRequested)
        {
            try
            {
                using var channel = GrpcChannel.ForAddress(BackendAddress);
                var client = new RobotControlBridge.RobotControlBridgeClient(channel);

                var request = new SubscribeControlRequest
                {
                    RobotId = RobotId,
                    ClientId = BuildClientId(),
                };

                using var call = client.SubscribeControl(request, cancellationToken: token);
                Debug.Log($"Connected gRPC stream to backend: {BackendAddress}, robotId={RobotId}");

                while (await call.ResponseStream.MoveNext(token))
                {
                    incomingQueue.Enqueue(call.ResponseStream.Current);
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                Debug.LogWarning($"gRPC stream disconnected: {ex.Message}. Reconnecting in {reconnectDelaySeconds:F1}s");
            }

            try
            {
                await Task.Delay(TimeSpan.FromSeconds(reconnectDelaySeconds), token);
            }
            catch (OperationCanceledException)
            {
                break;
            }
        }
    }

    private string BuildClientId()
    {
        if (!string.IsNullOrWhiteSpace(ClientId))
        {
            return ClientId;
        }

        return $"unity-{SystemInfo.deviceName}";
    }

    private void ApplyCommandOnMainThread(RobotControlCommand cmd)
    {
        if (cmd == null)
        {
            return;
        }

        if (cmd.JointAngles == null || cmd.JointAngles.Count < 6)
        {
            Debug.LogWarning("Received command without 6 joint angles");
            return;
        }

        float[] angles = new float[6];
        for (int i = 0; i < 6; i++)
        {
            angles[i] = (float)cmd.JointAngles[i];
        }

        if (cmd.HasGripper && useJoint5AsGripper)
        {
            angles[5] = cmd.Gripper == 1 ? gripperClosedAngle : gripperOpenAngle;
        }

        jointDriver.EnqueueAngles(angles);
    }
}
