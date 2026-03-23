using UnityEngine;

// Deprecated by design: keep this file only to avoid breaking old scenes.
// Use RobotArmGrpcClientBehaviour instead (Unity as gRPC client, Backend as gRPC server).
[System.Obsolete("Use RobotArmGrpcClientBehaviour. Unity should not host Grpc.Core.Server.")]
public class RobotArmGrpcServerBehaviour : MonoBehaviour
{
    private void Start()
    {
        Debug.LogWarning("RobotArmGrpcServerBehaviour is deprecated. Remove this component and use RobotArmGrpcClientBehaviour.");
    }
}
