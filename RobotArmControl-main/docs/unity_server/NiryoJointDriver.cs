using System.Collections.Concurrent;
using UnityEngine;

// Updated NiryoJointDriver: adds a thread-safe queue to receive angle arrays
// from a gRPC server running on a background thread. Call EnqueueAngles() from
// the gRPC service to update targets safely.
public class NiryoJointDriver : MonoBehaviour
{
    public enum DriveAxis { X, Y, Z }

    [Header("Assign 6 ArticulationBody joints in order J0..J5")]
    public ArticulationBody[] joints = new ArticulationBody[6];

    [Header("Target angles from Python / gRPC")]
    public float[] targetDeg = new float[6];

    [Header("Drive axis per joint")]
    public DriveAxis[] driveAxes = new DriveAxis[6]
    {
        DriveAxis.X, DriveAxis.X, DriveAxis.X,
        DriveAxis.X, DriveAxis.X, DriveAxis.X
    };

    [Header("Optional invert per joint")]
    public bool[] invert = new bool[6];

    [Header("Joint limits (deg)")]
    public float[] minDeg = new float[6] { -175f, -45f, -60f, -90f, -90f, -90f };
    public float[] maxDeg = new float[6] { 175f, 45f, 60f, 90f, 90f, 90f };

    [Header("Drive tuning")]
    public float stiffness = 10000f;
    public float damping = 500f;
    public float forceLimit = 10000f;
    public float speedDegPerSec = 120f;

    [Header("Keyboard manual speed")]
    public float manualSpeedDegPerSec = 40f;

    [Header("Fix: Wake sleeping joints")]
    public bool wakeUpOnTargetSet = true;

    [Header("Debug")]
    public bool debugLog = true;
    public bool clampTargets = true;

    private float[] currentDriveTargets;
    private bool driveSettingsInitialized = false;

    // Thread-safe queue to receive angle arrays from background gRPC thread
    private readonly ConcurrentQueue<float[]> incomingAngleQueue = new ConcurrentQueue<float[]>();

    void Awake()
    {
        // Ensure arrays are the correct length before any FixedUpdate/Update runs
        ValidateArrayLengths();

        if (joints == null)
            joints = new ArticulationBody[6];

        if (currentDriveTargets == null || currentDriveTargets.Length != joints.Length)
            currentDriveTargets = new float[joints.Length];
    }

    void Start()
    {
        ValidateArrayLengths();

        currentDriveTargets = new float[joints.Length];

        for (int i = 0; i < joints.Length; i++)
        {
            var j = joints[i];
            if (j == null)
            {
                Debug.LogWarning($"Joint {i} is null");
                continue;
            }

            if (j.jointType == ArticulationJointType.FixedJoint)
            {
                Debug.LogWarning($"Joint {i} ({j.name}) is FixedJoint -> will not move");
            }

            InitializeDriveSettings(j, i);

            float t = ApplyJointSettings(i, targetDeg[i]);
            SetDriveTarget(j, i, t);
            currentDriveTargets[i] = t;
            targetDeg[i] = t;

            if (debugLog)
            {
                Debug.Log($"Init J{i}: {j.name}, axis={driveAxes[i]}, target={t:F2}");
            }
        }

        driveSettingsInitialized = true;
    }

    void ValidateArrayLengths()
    {
        int n = joints.Length;

        if (targetDeg == null || targetDeg.Length != n)
            System.Array.Resize(ref targetDeg, n);

        if (driveAxes == null || driveAxes.Length != n)
            System.Array.Resize(ref driveAxes, n);

        if (invert == null || invert.Length != n)
            System.Array.Resize(ref invert, n);

        if (minDeg == null || minDeg.Length != n)
            System.Array.Resize(ref minDeg, n);

        if (maxDeg == null || maxDeg.Length != n)
            System.Array.Resize(ref maxDeg, n);
    }

    void FixedUpdate()
    {
        // Drain incoming angle queue (only keep newest frame)
        bool got = false;
        float[] latest = null;
        while (incomingAngleQueue.TryDequeue(out var arr))
        {
            latest = arr;
            got = true;
        }

        if (got && latest != null)
        {
            int n = Mathf.Min(latest.Length, targetDeg.Length);
            for (int i = 0; i < n; i++) targetDeg[i] = latest[i];
        }

        HandleKeyboardInput();

        for (int i = 0; i < joints.Length; i++)
        {
            var j = joints[i];
            if (j == null) continue;

            float desired = ApplyJointSettings(i, targetDeg[i]);
            float current = currentDriveTargets[i];

            if (Mathf.Approximately(current, desired))
                continue;

            float next = Mathf.MoveTowards(
                current,
                desired,
                speedDegPerSec * Time.fixedDeltaTime
            );

            SetDriveTarget(j, i, next);
            currentDriveTargets[i] = next;

            if (debugLog)
            {
                Debug.Log($"J{i} {j.name} axis={driveAxes[i]} desired={desired:F2}, current={current:F2}, next={next:F2}");
            }
        }
    }

    float ApplyJointSettings(int index, float value)
    {
        float v = value;

        if (invert != null && index < invert.Length && invert[index])
            v = -v;

        if (clampTargets && minDeg != null && maxDeg != null &&
            index < minDeg.Length && index < maxDeg.Length)
        {
            v = Mathf.Clamp(v, minDeg[index], maxDeg[index]);
        }

        return v;
    }

    ArticulationDrive GetDrive(ArticulationBody joint, int index)
    {
        return driveAxes[index] switch
        {
            DriveAxis.Y => joint.yDrive,
            DriveAxis.Z => joint.zDrive,
            _ => joint.xDrive
        };
    }

    void SetDrive(ArticulationBody joint, int index, ArticulationDrive drive)
    {
        switch (driveAxes[index])
        {
            case DriveAxis.Y:
                joint.yDrive = drive;
                break;
            case DriveAxis.Z:
                joint.zDrive = drive;
                break;
            default:
                joint.xDrive = drive;
                break;
        }
    }

    void InitializeDriveSettings(ArticulationBody joint, int index)
    {
        if (joint == null) return;

        var drive = GetDrive(joint, index);
        drive.stiffness = stiffness;
        drive.damping = damping;
        drive.forceLimit = forceLimit;
        SetDrive(joint, index, drive);
    }

    void SetDriveTarget(ArticulationBody joint, int index, float target)
    {
        if (joint == null) return;

        var drive = GetDrive(joint, index);

        if (!driveSettingsInitialized)
        {
            drive.stiffness = stiffness;
            drive.damping = damping;
            drive.forceLimit = forceLimit;
        }

        drive.target = target;
        SetDrive(joint, index, drive);

        if (wakeUpOnTargetSet)
            joint.WakeUp();
    }

    void HandleKeyboardInput()
    {
        if (UnityEngine.InputSystem.Keyboard.current == null) return;

        float step = manualSpeedDegPerSec * Time.fixedDeltaTime;

        if (UnityEngine.InputSystem.Keyboard.current.digit1Key.isPressed) targetDeg[0] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.digit2Key.isPressed) targetDeg[0] += step;
        if (UnityEngine.InputSystem.Keyboard.current.digit3Key.isPressed) targetDeg[1] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.digit4Key.isPressed) targetDeg[1] += step;
        if (UnityEngine.InputSystem.Keyboard.current.digit5Key.isPressed) targetDeg[2] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.digit6Key.isPressed) targetDeg[2] += step;
        if (UnityEngine.InputSystem.Keyboard.current.digit7Key.isPressed) targetDeg[3] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.digit8Key.isPressed) targetDeg[3] += step;
        if (UnityEngine.InputSystem.Keyboard.current.digit9Key.isPressed) targetDeg[4] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.digit0Key.isPressed) targetDeg[4] += step;
        if (UnityEngine.InputSystem.Keyboard.current.minusKey.isPressed) targetDeg[5] -= step;
        if (UnityEngine.InputSystem.Keyboard.current.equalsKey.isPressed) targetDeg[5] += step;
    }

    public void SetTargets(float[] angles)
    {
        if (angles == null) return;

        int n = Mathf.Min(angles.Length, targetDeg.Length);
        for (int i = 0; i < n; i++)
        {
            targetDeg[i] = angles[i];
        }
    }

    public void SetTarget(int jointIndex, float angleDeg)
    {
        if (jointIndex < 0 || jointIndex >= targetDeg.Length) return;
        targetDeg[jointIndex] = angleDeg;
    }

    public float[] GetCurrentAngles()
    {
        float[] angles = new float[joints.Length];
        for (int i = 0; i < joints.Length; i++)
        {
            if (joints[i] != null && currentDriveTargets != null && i < currentDriveTargets.Length)
                angles[i] = currentDriveTargets[i];
            else
                angles[i] = 0f;
        }
        return angles;
    }

    [ContextMenu("Reset Targets To Zero")]
    public void ResetTargets()
    {
        for (int i = 0; i < targetDeg.Length; i++)
            targetDeg[i] = 0f;
    }

    public void UpdateDriveSettings(float newStiffness, float newDamping, float newForceLimit)
    {
        stiffness = newStiffness;
        damping = newDamping;
        forceLimit = newForceLimit;

        for (int i = 0; i < joints.Length; i++)
        {
            if (joints[i] != null)
                InitializeDriveSettings(joints[i], i);
        }
    }

    // Public API to be called from gRPC service (background thread)
    public void EnqueueAngles(float[] angles)
    {
        if (angles == null) return;
        // copy to avoid sharing array references
        float[] copy = new float[Mathf.Min(angles.Length, targetDeg.Length)];
        System.Array.Copy(angles, copy, copy.Length);
        incomingAngleQueue.Enqueue(copy);
    }
}
