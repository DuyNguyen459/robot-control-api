using System;
using System.Collections.Concurrent;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;
using RobotControl.Grpc;
using UnityEngine;

// Unity-side gRPC client: subscribes to Backend stream and pushes commands to NiryoJointDriver.
// This avoids hosting Grpc.Core.Server inside Unity and therefore avoids grpc_csharp_ext native-plugin issues.
public class RobotArmGrpcClientBehaviour : MonoBehaviour
{
    [Header("Assign robot driver component (NiryoJointDriver1 or compatible)")]
    public MonoBehaviour jointDriverBehaviour;

    [Header("Backend gRPC endpoint")]
    public string BackendAddress = "http://127.0.0.1:50061";
    public bool forceTls = false;
    public bool forceInsecure = false;

    [Header("Subscription")]
    public long RobotId = 1;
    public string ClientId = "unity-client";

    [Header("Optional gripper mapping")]
    public bool useJoint5AsGripper = true;
    public float gripperOpenAngle = 0f;
    public float gripperClosedAngle = 45f;

    [Header("Reconnect")]
    public float reconnectDelaySeconds = 2f;

    [Header("Diagnostics")]
    public bool enableConsoleLog = true;
    public bool enableFileLog = true;
    public string logFileName = "robot_grpc_client.log";
    public bool verbosePayloadLog = false;
    public float statusLogIntervalSeconds = 5f;

    private readonly ConcurrentQueue<RobotControlCommand> incomingQueue = new ConcurrentQueue<RobotControlCommand>();
    private CancellationTokenSource cts;

    private long receivedCount;
    private long appliedCount;
    private string logFilePath;
    private float lastStatusLogTime;
    private System.Reflection.MethodInfo enqueueAnglesMethod;

    private void Start()
    {
        if (!TryResolveDriver())
        {
            WriteLog("ERROR", "Cannot find any component exposing EnqueueAngles(float[]). Assign NiryoJointDriver1 in Inspector.");
            enabled = false;
            return;
        }

        logFilePath = Path.Combine(Application.persistentDataPath, logFileName);
        WriteLog("INFO", "RobotArmGrpcClientBehaviour starting");
        WriteLog("INFO", $"BackendAddress={BackendAddress}, RobotId={RobotId}, ClientId={BuildClientId()}");
        WriteLog("INFO", $"Driver component={jointDriverBehaviour.GetType().Name}");
        WriteLog("INFO", $"Log file path: {logFilePath}");

        cts = new CancellationTokenSource();
        _ = Task.Run(() => SubscribeLoopAsync(cts.Token));
    }

    private bool TryResolveDriver()
    {
        // 1) Use explicit assignment if valid.
        if (jointDriverBehaviour != null)
        {
            var method = jointDriverBehaviour.GetType().GetMethod("EnqueueAngles", new[] { typeof(float[]) });
            if (method != null)
            {
                enqueueAnglesMethod = method;
                WriteLog("INFO", $"Using assigned driver: {jointDriverBehaviour.GetType().Name}");
                return true;
            }

            WriteLog("WARN", $"Assigned component '{jointDriverBehaviour.GetType().Name}' is not compatible, trying auto-discovery...");
        }

        // 2) Auto-discover best candidate in scene.
        MonoBehaviour fallback = null;
        var allBehaviours = UnityEngine.Object.FindObjectsByType<MonoBehaviour>(FindObjectsSortMode.None);
        foreach (var behaviour in allBehaviours)
        {
            if (behaviour == null) continue;
            var method = behaviour.GetType().GetMethod("EnqueueAngles", new[] { typeof(float[]) });
            if (method == null) continue;

            // Prefer NiryoJointDriver1 by class name for your current project.
            if (string.Equals(behaviour.GetType().Name, "NiryoJointDriver1", StringComparison.Ordinal))
            {
                jointDriverBehaviour = behaviour;
                enqueueAnglesMethod = method;
                WriteLog("INFO", $"Auto-resolved driver (preferred): {behaviour.GetType().Name} on {behaviour.gameObject.name}");
                return true;
            }

            if (fallback == null)
            {
                fallback = behaviour;
            }
        }

        if (fallback != null)
        {
            jointDriverBehaviour = fallback;
            enqueueAnglesMethod = fallback.GetType().GetMethod("EnqueueAngles", new[] { typeof(float[]) });
            WriteLog("INFO", $"Auto-resolved driver (fallback): {fallback.GetType().Name} on {fallback.gameObject.name}");
            return true;
        }

        return false;
    }

    private void OnDestroy()
    {
        WriteLog("INFO", "RobotArmGrpcClientBehaviour stopping");
        if (cts != null)
        {
            cts.Cancel();
            cts.Dispose();
            cts = null;
        }
    }

    private void Update()
    {
        if (Time.time - lastStatusLogTime >= statusLogIntervalSeconds)
        {
            lastStatusLogTime = Time.time;
            WriteLog("INFO", $"status: received={receivedCount}, applied={appliedCount}, queue={incomingQueue.Count}");
        }

        while (incomingQueue.TryDequeue(out var cmd))
        {
            ApplyCommandOnMainThread(cmd);
        }
    }

    private async Task SubscribeLoopAsync(CancellationToken token)
    {
        while (!token.IsCancellationRequested)
        {
            Channel channel = null;
            try
            {
                if (!TryParseHostPort(BackendAddress, out var host, out var port, out var schemeSuggestsTls))
                {
                    WriteLog("ERROR", $"Invalid BackendAddress '{BackendAddress}'. Expected formats: '127.0.0.1:50061', 'http://127.0.0.1:50061', or 'https://robot-control-api-1.onrender.com:443'");
                    return;
                }

                bool useTls = ResolveTlsMode(port, schemeSuggestsTls);
                channel = new Channel(host, port, useTls ? (ChannelCredentials)new SslCredentials() : ChannelCredentials.Insecure);
                var client = new RobotControlBridge.RobotControlBridgeClient(channel);

                var request = new SubscribeControlRequest
                {
                    RobotId = RobotId,
                    ClientId = BuildClientId(),
                };

                using var call = client.SubscribeControl(request, cancellationToken: token);
                WriteLog("INFO", $"Connected gRPC stream to backend: {BackendAddress}, robotId={RobotId}, tls={useTls}");

                while (await call.ResponseStream.MoveNext(token))
                {
                    receivedCount += 1;
                    if (verbosePayloadLog)
                    {
                        WriteLog("DEBUG", $"received payload #{receivedCount}: {FormatPayload(call.ResponseStream.Current)}");
                    }
                    incomingQueue.Enqueue(call.ResponseStream.Current);
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                WriteLog("WARN", $"gRPC stream disconnected: {ex.Message}. Reconnecting in {reconnectDelaySeconds:F1}s");
            }
            finally
            {
                if (channel != null)
                {
                    try
                    {
                        await channel.ShutdownAsync();
                    }
                    catch
                    {
                        // ignore channel shutdown errors
                    }
                }
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

    private bool TryParseHostPort(string address, out string host, out int port, out bool schemeSuggestsTls)
    {
        host = null;
        port = 0;
        schemeSuggestsTls = false;

        if (string.IsNullOrWhiteSpace(address))
        {
            return false;
        }

        string raw = address.Trim();
        if (raw.StartsWith("http://", StringComparison.OrdinalIgnoreCase))
        {
            raw = raw.Substring("http://".Length);
        }
        else if (raw.StartsWith("https://", StringComparison.OrdinalIgnoreCase))
        {
            schemeSuggestsTls = true;
            raw = raw.Substring("https://".Length);
        }

        raw = raw.TrimEnd('/');
        var parts = raw.Split(':');
        if (parts.Length != 2)
        {
            return false;
        }

        host = parts[0].Trim();
        if (string.IsNullOrWhiteSpace(host))
        {
            return false;
        }

        if (!int.TryParse(parts[1], out port) || port <= 0 || port > 65535)
        {
            return false;
        }

        return true;
    }

    private bool ResolveTlsMode(int port, bool schemeSuggestsTls)
    {
        if (forceTls && forceInsecure)
        {
            WriteLog("WARN", "Both forceTls and forceInsecure are enabled. Falling back to TLS.");
            return true;
        }

        if (forceTls)
        {
            return true;
        }

        if (forceInsecure)
        {
            return false;
        }

        return schemeSuggestsTls || port == 443;
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

        // Log chi tiết toàn bộ payload nhận được từ BE/gRPC
        WriteLog("INFO", $"[RAW CMD] {FormatPayload(cmd)}");

        if (cmd == null)
        {
            WriteLog("WARN", "received null command");
            return;
        }

        if (cmd.JointAngles == null || cmd.JointAngles.Count < 6)
        {
            WriteLog("WARN", "received command without 6 joint angles");
            return;
        }

        float[] angles = new float[6];
        for (int i = 0; i < 6; i++)
        {
            angles[i] = (float)cmd.JointAngles[i];
        }


        // Log và luôn tìm RobotGrabber trong scene để gọi đúng hành động vật lý
        if (cmd.HasGripper)
        {
            string gripperAction = cmd.Gripper == 1 ? "GRIP (gắp)" : "RELEASE (thả)";
            WriteLog("INFO", $"[GRIPPER] Nhận lệnh: {gripperAction} (cmd.Gripper={cmd.Gripper})");

            string actionStr = cmd.Gripper == 1 ? "grab" : "release";
            // Ưu tiên tìm trên cùng GameObject
            var grabber = GetComponent<RobotGrabber>();
            if (grabber == null)
            {
                // Nếu không có, tìm bất kỳ RobotGrabber nào trong scene
                grabber = GameObject.FindObjectOfType<RobotGrabber>();
            }
            if (grabber != null)
            {
                WriteLog("INFO", $"[GRIPPER] Gọi RobotGrabber.HandleAction('{actionStr}') trên {grabber.gameObject.name}");
                grabber.HandleAction(actionStr);
            }
            else
            {
                WriteLog("WARN", "[GRIPPER] Không tìm thấy RobotGrabber nào trong scene để thực hiện hành động vật lý!");
            }
        }

        if (cmd.HasGripper && useJoint5AsGripper)
        {
            angles[5] = cmd.Gripper == 1 ? gripperClosedAngle : gripperOpenAngle;
        }

        try
        {
            enqueueAnglesMethod.Invoke(jointDriverBehaviour, new object[] { angles });
        }
        catch (Exception ex)
        {
            WriteLog("ERROR", $"Failed to invoke EnqueueAngles on driver: {ex.Message}");
            return;
        }

        appliedCount += 1;

        if (verbosePayloadLog)
        {
            WriteLog("DEBUG", $"applied payload #{appliedCount}: [{string.Join(", ", angles.Select(a => a.ToString("F2")))}]");
        }
    }

    private string FormatPayload(RobotControlCommand cmd)
    {
        var angles = cmd?.JointAngles == null
                ? "null"
                : string.Join(", ", cmd.JointAngles.Select(a => a.ToString("F2")));

        return $"robotId={cmd.RobotId}, hasGripper={cmd.HasGripper}, gripper={cmd.Gripper}, ts={cmd.Timestamp}, angles=[{angles}]";
    }

    private void WriteLog(string level, string message)
    {
        string line = $"[{DateTime.UtcNow:O}] [{level}] {message}";

        if (enableConsoleLog)
        {
            if (level == "ERROR") Debug.LogError(line);
            else if (level == "WARN") Debug.LogWarning(line);
            else Debug.Log(line);
        }

        if (!enableFileLog)
        {
            return;
        }

        try
        {
            if (string.IsNullOrWhiteSpace(logFilePath))
            {
                logFilePath = Path.Combine(Application.persistentDataPath, logFileName);
            }

            File.AppendAllText(logFilePath, line + Environment.NewLine);
        }
        catch
        {
            // Do not break runtime if file logging fails.
        }
    }
}
