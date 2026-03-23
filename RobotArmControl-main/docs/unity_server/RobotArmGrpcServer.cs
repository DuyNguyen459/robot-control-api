using System;
using System.Threading.Tasks;
using Grpc.Core;
using RobotArm.Grpc;

namespace RobotArmServer
{
    // Implement the generated base service. Unity can host this server or run it as a separate process.
public class RobotArmServiceImpl : RobotArm.RobotArmBase
    {
        public override async Task<StreamAck> StreamLandmarks(IAsyncStreamReader<Landmark> requestStream, ServerCallContext context)
        {
            while (await requestStream.MoveNext())
            {
                var lm = requestStream.Current;
                // lm.Id, lm.X, lm.Y, lm.Z, lm.Timestamp are available directly.
                Console.WriteLine($"Landmark {lm.Id}: x={lm.X} y={lm.Y} z={lm.Z} ts={lm.Timestamp}");

                // In Unity, dispatch this to the main thread and map to robot joints.
                // Example (pseudocode): RobotArmController.Instance.SetTarget(lm.X, lm.Y, lm.Z);
            }

            return new StreamAck { Ok = true, Message = "Stream received" };
        }
    }

    class Program
    {
        const int Port = 50051;

        static void Main(string[] args)
        {
            Server server = new Server
            {
                Services = { RobotArm.BindService(new RobotArmServiceImpl()) },
                Ports = { new ServerPort("0.0.0.0", Port, ServerCredentials.Insecure) }
            };

            server.Start();
            Console.WriteLine($"gRPC server listening on port {Port}");
            Console.WriteLine("Press Enter to stop");
            Console.ReadLine();

            server.ShutdownAsync().Wait();
        }
    }
}
