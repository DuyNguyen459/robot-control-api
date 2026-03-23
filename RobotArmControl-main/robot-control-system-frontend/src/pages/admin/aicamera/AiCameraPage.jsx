import React, { useEffect, useMemo, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAiCamera } from "../../../hooks/useAiCamera";

function Pill({ label, ok }) {
  return (
      <span
          className={[
            "inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium",
            ok ? "bg-green-500/15 text-green-300" : "bg-white/10 text-white/60",
          ].join(" ")}
      >
      {label}: {ok ? "Yes" : "No"}
    </span>
  );
}

export default function AiCameraPage() {
  const navigate = useNavigate();

  const {
    videoRef,
    role,
    isViewer,
    wsConnected,
    sessionActive,
    sessionDeviceId,
    isCameraRunning,
    isSendingAngles,
    angles,
    error,
    startSession,
    endSession,
    startCamera,
    stopCamera,
    selectedJoint,
    fingersCount,
    debugRef,
    deadzone,
    selfieMode,
  } = useAiCamera();

  const selectedDeviceId = (() => {
    const raw = sessionStorage.getItem("robotSession.deviceId");
    if (raw == null || String(raw).trim() === "") return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  })();

  const activeDeviceId = sessionDeviceId ?? selectedDeviceId;

  async function handleEndSession() {
    const stopped = await endSession();
    if (stopped?.sessionId != null) {
      navigate("/admin/dashboard");
    }
  }

  const startSessionDisabled = isViewer || sessionActive || selectedDeviceId == null;
  const endSessionDisabled = isViewer || !sessionActive;

  const startCameraDisabled = isViewer || !sessionActive || isCameraRunning || isSendingAngles;
  const stopCameraDisabled = isViewer || (!isCameraRunning && !isSendingAngles);

  const jointLabel = `J${selectedJoint}`;

  const canvasRef = useRef(null);

  const jointNames = useMemo(
      () => [
        "J0 shoulder_link",
        "J1 arm_link",
        "J2 elbow_link",
        "J3 forearm_link",
        "J4 wrist_link",
        "J5 hand_link",
      ],
      []
  );

  useEffect(() => {
    let raf = 0;

    const canvas = canvasRef.current;
    const video = videoRef.current;
    if (!canvas || !video) return undefined;

    const ctx = canvas.getContext("2d");
    if (!ctx) return undefined;

    const connections = [
      [0, 1],
      [1, 2],
      [2, 3],
      [3, 4],
      [0, 5],
      [5, 6],
      [6, 7],
      [7, 8],
      [0, 9],
      [9, 10],
      [10, 11],
      [11, 12],
      [0, 13],
      [13, 14],
      [14, 15],
      [15, 16],
      [0, 17],
      [17, 18],
      [18, 19],
      [19, 20],
      [5, 9],
      [9, 13],
      [13, 17],
    ];

    const ensureSize = () => {
      const w = video.videoWidth || 0;
      const h = video.videoHeight || 0;
      if (w > 0 && h > 0) {
        if (canvas.width !== w) canvas.width = w;
        if (canvas.height !== h) canvas.height = h;
      }
    };

    const drawLineX = (x01, color, width = 1) => {
      if (x01 == null) return;
      const x01Draw = selfieMode ? 1 - x01 : x01;
      const x = Math.round(x01Draw * canvas.width);
      ctx.save();
      ctx.strokeStyle = color;
      ctx.lineWidth = width;
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, canvas.height);
      ctx.stroke();
      ctx.restore();
    };

    const drawText = (text, x, y, color, size = 18, weight = "600") => {
      ctx.save();
      ctx.fillStyle = color;
      ctx.font = `${weight} ${size}px system-ui, -apple-system, Segoe UI, Roboto, Arial`;
      ctx.fillText(text, x, y);
      ctx.restore();
    };

    const frame = () => {
      ensureSize();

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const dbg = debugRef?.current;
      const lm = dbg?.landmarks;

      // Guide lines (match Python feel)
      drawLineX(0.5, "rgba(255,255,255,0.85)", 1);
      drawLineX(0.5 - (deadzone ?? 0.08), "rgba(120,120,255,0.85)", 1);
      drawLineX(0.5 + (deadzone ?? 0.08), "rgba(120,120,255,0.85)", 1);
      drawLineX(dbg?.palmX, "rgba(255,220,0,0.95)", 2);

      if (Array.isArray(lm) && lm.length >= 21) {
        // Connections
        ctx.save();
        ctx.strokeStyle = "rgba(0,255,0,0.9)";
        ctx.lineWidth = 2;
        for (const [a, b] of connections) {
          const p1 = lm[a];
          const p2 = lm[b];
          if (!p1 || !p2) continue;
          const x1 = (selfieMode ? 1 - p1.x : p1.x) * canvas.width;
          const y1 = p1.y * canvas.height;
          const x2 = (selfieMode ? 1 - p2.x : p2.x) * canvas.width;
          const y2 = p2.y * canvas.height;
          ctx.beginPath();
          ctx.moveTo(x1, y1);
          ctx.lineTo(x2, y2);
          ctx.stroke();
        }
        ctx.restore();

        // Points
        ctx.save();
        ctx.fillStyle = "rgba(40,120,255,0.95)";
        for (const p of lm) {
          if (!p) continue;
          const x = (selfieMode ? 1 - p.x : p.x) * canvas.width;
          const y = p.y * canvas.height;
          ctx.beginPath();
          ctx.arc(x, y, 5, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.restore();
      }

      // HUD text
      let handLabel = dbg?.handednessRaw || "Unknown";
      if (selfieMode && handLabel === "Left") handLabel = "Right";
      else if (selfieMode && handLabel === "Right") handLabel = "Left";
      const control = Number(dbg?.control ?? 0);
      drawText(`Hand: ${handLabel}`, 12, 26, "rgba(0,255,255,0.95)");
      drawText(`Fingers: ${fingersCount}`, 12, 52, "rgba(0,255,255,0.95)");
      drawText(`Selected: ${jointNames[selectedJoint] || jointLabel}`, 12, 80, "rgba(255,220,0,0.95)", 18);
      drawText(`Control: ${control >= 0 ? "+" : ""}${control.toFixed(2)}`, 12, 106, "rgba(255,180,0,0.95)");

      // Angles list
      let y = 140;
      for (let i = 0; i < jointNames.length; i += 1) {
        const a = Number(angles?.[i] ?? 0);
        const color = i === selectedJoint ? "rgba(255,220,0,0.95)" : "rgba(0,255,0,0.9)";
        drawText(`${jointNames[i]}: ${a.toFixed(1)}`, 12, y, color, 16, "600");
        y += 26;
      }

      // Corner state
      if (isSendingAngles) {
        drawText("ACTIVE", canvas.width - 120, 32, "rgba(0,255,0,0.95)", 22, "800");
      }

      raf = requestAnimationFrame(frame);
    };

    raf = requestAnimationFrame(frame);
    return () => {
      if (raf) cancelAnimationFrame(raf);
    };
  }, [angles, deadzone, debugRef, fingersCount, isSendingAngles, jointLabel, jointNames, selectedJoint, selfieMode, videoRef]);

  return (
      <div className="w-full">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-semibold text-white text-left">AI Camera Control</h1>
            <p className="mt-1 text-sm text-white/60">
              Control robot joints using browser webcam hand tracking
            </p>
            <div className="mt-2 text-xs text-white/50">Role: {role || "—"}</div>
            <div className="mt-1 text-xs text-white/50">Selected Device ID: {selectedDeviceId ?? "—"}</div>
            <div className="mt-1 text-xs text-white/50">Session Device ID: {activeDeviceId ?? "—"}</div>

            {isViewer && (
                <div className="mt-3 rounded-lg border border-yellow-500/20 bg-yellow-500/10 px-3 py-2 text-sm text-yellow-300">
                  Viewer cannot control robot camera
                </div>
            )}

            {error && (
                <div className="mt-3 rounded-lg border border-red-500/20 bg-red-500/10 px-3 py-2 text-sm text-red-300">
                  {error}
                </div>
            )}
          </div>

          <div className="flex items-center gap-2 flex-wrap justify-end">
            <button
                type="button"
                onClick={startSession}
                disabled={startSessionDisabled}
                className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            >
              Start Session
            </button>
            <button
                type="button"
                onClick={handleEndSession}
                disabled={endSessionDisabled}
                className="h-10 px-4 rounded-lg bg-white/10 text-white hover:bg-white/15 transition disabled:opacity-60"
            >
              End Session
            </button>
            <button
                type="button"
                onClick={startCamera}
                disabled={startCameraDisabled}
                className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            >
              Start AI Camera
            </button>
            <button
                type="button"
                onClick={stopCamera}
                disabled={stopCameraDisabled}
                className="h-10 px-4 rounded-lg bg-white/10 text-white hover:bg-white/15 transition disabled:opacity-60"
            >
              Stop AI Camera
            </button>
          </div>
        </div>

        <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div className="rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
            <div className="px-5 py-4 border-b border-white/10">
              <div className="text-xs uppercase tracking-wider text-white/50">Webcam Preview</div>
            </div>
            <div className="p-5">
              <div className="aspect-video w-full rounded-xl bg-black/40 border border-white/10 overflow-hidden">
                <div className="relative h-full w-full">
                  <video
                      ref={videoRef}
                      className="absolute inset-0 h-full w-full object-cover"
                      autoPlay
                      playsInline
                      muted
                      style={{ transform: "scaleX(-1)" }}
                  />
                  <canvas
                      ref={canvasRef}
                      className="absolute inset-0 h-full w-full pointer-events-none"
                  />
                </div>
              </div>
              <div className="mt-2 text-xs text-white/50">
                If permission is denied, allow camera access in the browser.
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
            <div className="px-5 py-4 border-b border-white/10">
              <div className="text-xs uppercase tracking-wider text-white/50">Status</div>
            </div>

            <div className="p-5 space-y-4">
              <div className="flex flex-wrap gap-2">
                <Pill label="Session Active" ok={sessionActive} />
                <Pill label="Camera Running" ok={isCameraRunning} />
                <Pill label="Sending Angles" ok={isSendingAngles} />
              </div>

              <div className="text-xs text-white/60">Fingers: {fingersCount} · Selected: {jointLabel}</div>

              <div className="rounded-xl border border-white/10 bg-neutral-950/30 overflow-hidden">
                <div className="px-4 py-3 border-b border-white/10">
                  <div className="text-xs uppercase tracking-wider text-white/50">Live Angles (degrees)</div>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                    <tr className="text-xs uppercase tracking-wider text-white/40">
                      <th className="text-left font-medium px-4 py-3">Joint</th>
                      <th className="text-left font-medium px-4 py-3">Index</th>
                      <th className="text-left font-medium px-4 py-3">Angle</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-white/10">
                    {[
                      "shoulder_link",
                      "arm_link",
                      "elbow_link",
                      "forearm_link",
                      "wrist_link",
                      "hand_link",
                    ].map((name, idx) => (
                        <tr key={name} className="hover:bg-white/5 transition">
                          <td className="px-4 py-3 text-white">{name}</td>
                          <td className="px-4 py-3 text-white/70">{idx}</td>
                          <td className="px-4 py-3 text-white/70">
                            {Number(angles?.[idx] ?? 0).toFixed(1)}
                          </td>
                        </tr>
                    ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="text-xs text-white/50">
                REST payload sent: {`{ "robotId": 1, "jointAngles": [a0..a5], "gripper": 0|1, "timestamp": "ISO-8601" }`}
              </div>
              <div className="text-xs text-white/50">
                Gripper action uses pinch gestures to avoid conflicting with joint finger-count mode: thumb-index pinch = grab, thumb-middle pinch = release.
              </div>
              <div className="text-xs text-white/50">Backend forwards each payload to Unity via gRPC stream.</div>
            </div>
          </div>
        </div>
      </div>
  );
}
