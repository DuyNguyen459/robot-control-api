using System;
using System.Threading.Tasks;
using Grpc.Core;
using RobotArm.Grpc;
using UnityEngine;

// MonoBehaviour that runs a gRPC server to accept Landmark streams from Python.
// Add this script to a GameObject and assign the NiryoJointDriver reference.
public class RobotArmGrpcServerBehaviour : MonoBehaviour
{
    public NiryoJointDriver jointDriver;
    public int Port = 50051;

    private Server server;

    void Start()
    {
        if (jointDriver == null)
        {
            Debug.LogError("RobotArmGrpcServerBehaviour: jointDriver not assigned");
            return;
        }

        Task.Run(() => StartServer());
    }

    void OnDestroy()
    {
        if (server != null)
        {
            try
            {
                server.ShutdownAsync().Wait(1000);
            }
            catch (Exception ex)
            {
                Debug.LogWarning("Error shutting down gRPC server: " + ex.Message);
            }
        }
    }

    private void StartServer()
    {
        server = new Server
        {
            Services = { RobotArm.RobotArm.BindService(new ServiceImpl(jointDriver)) },
            Ports = { new ServerPort("0.0.0.0", Port, ServerCredentials.Insecure) }
        };
        server.Start();
        Debug.Log($"RobotArm gRPC server listening on port {Port}");
    }

    // Service implementation that runs on gRPC threads and must not touch Unity objects directly.
    private class ServiceImpl : RobotArm.RobotArmBase
    {
        private readonly NiryoJointDriver driver;

        public ServiceImpl(NiryoJointDriver driver)
        {
            this.driver = driver;
        }

        public override async Task<StreamAck> StreamLandmarks(IAsyncStreamReader<Landmark> requestStream, ServerCallContext context)
        {
            // Simple mapping: landmarks with id 0..5 map to joints 0..5, using the X field as degrees.
            // If your landmarks are normalized (0..1) apply a scale factor before enqueueing.
            float[] angles = new float[6];

            try
            {
                while (await requestStream.MoveNext())
                {
                    var lm = requestStream.Current;
                    int id = (int)lm.Id;
                    if (id >= 0 && id < angles.Length)
                    {
                        // Direct mapping: use lm.X as degrees. Adjust if your sender uses normalized coords.
                        angles[id] = (float)lm.X;
                    }

                    // After each received landmark we push the array to Unity main thread via EnqueueAngles.
                    // EnqueueAngles is thread-safe and will be applied in FixedUpdate.
                    driver.EnqueueAngles(angles);
                }
            }
            catch (RpcException ex)
            {
                Debug.LogWarning($"gRPC StreamLandmarks ended with RPC error: {ex.Status}");
            }
            catch (Exception ex)
            {
                Debug.LogWarning($"gRPC StreamLandmarks error: {ex.Message}");
            }

            return new StreamAck { Ok = true, Message = "Stream closed" };
        }
    }
}
