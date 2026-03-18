import { useEffect, useMemo, useRef, useState } from "react";
import { Hands } from "@mediapipe/hands";
import { cameraService } from "../api/cameraService";
import { logService } from "../api/logService";
import { buildWsUrl, WebsocketService } from "../services/websocketService";

const JOINT_LIMITS = [
  [-175, 175], // J0 shoulder_link
  [-45, 45],   // J1 arm_link
  [-60, 60],   // J2 elbow_link
  [-90, 90],   // J3 forearm_link
  [-90, 90],   // J4 wrist_link
  [-90, 90],   // J5 hand_link
];

const SPEEDS_DEG_PER_SEC = [120, 90, 90, 100, 100, 100];
const MODE_HOLD_MS = 300;

const SELFIE_MODE = String(import.meta.env.VITE_AI_CAMERA_SELFIE_MODE || "1") !== "0";
const DEADZONE = 0.08;
const MAX_OFFSET = 0.35;

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

function normalizePalmX(palmX) {
  // Map palmX (0..1) -> control (-1..1) with a small deadzone
  const controlSignal = palmX - 0.5;
  const deadzone = DEADZONE;
  const maxOffset = MAX_OFFSET;

  if (Math.abs(controlSignal) < deadzone) return 0;
  const sign = controlSignal > 0 ? 1 : -1;
  const mag = (Math.abs(controlSignal) - deadzone) / (maxOffset - deadzone);
  return clamp(sign * mag, -1, 1);
}

function computePalmCenterX(landmarks) {
  // Average of wrist + MCPs: 0,5,9,13,17
  const ids = [0, 5, 9, 13, 17];
  let sum = 0;
  let count = 0;
  for (const i of ids) {
    const lm = landmarks?.[i];
    if (!lm) continue;
    sum += lm.x;
    count += 1;
  }
  return count ? sum / count : null;
}

function maybeMirror01(x) {
  if (x == null) return null;
  return SELFIE_MODE ? 1 - x : x;
}

function countFingers(landmarks, handedness) {
  if (!landmarks || landmarks.length < 21) return 0;

  let fingers = 0;
  const FINGER_OPEN_THRESHOLD = 0.015;

  // Trỏ
  if (landmarks[8].y < landmarks[6].y - FINGER_OPEN_THRESHOLD) fingers += 1;
  // Giữa
  if (landmarks[12].y < landmarks[10].y - FINGER_OPEN_THRESHOLD) fingers += 1;
  // Áp út
  if (landmarks[16].y < landmarks[14].y - FINGER_OPEN_THRESHOLD) fingers += 1;
  // Út
  if (landmarks[20].y < landmarks[18].y - FINGER_OPEN_THRESHOLD) fingers += 1;

  // Ngón cái giữ nguyên logic tối ưu như hiện tại
  const thumbDx = landmarks[4].x - landmarks[2].x;
  const thumbDy = Math.abs(landmarks[4].y - landmarks[2].y);
  let thumbOpen = false;
  if (handedness === "Right") {
    thumbOpen = thumbDx > 0.025 && thumbDy < 0.15;
  } else if (handedness === "Left") {
    thumbOpen = thumbDx < -0.025 && thumbDy < 0.15;
  } else {
    thumbOpen = Math.abs(thumbDx) > 0.035 && thumbDy < 0.15;
  }

  if (!thumbOpen) {
    thumbOpen = Math.abs(thumbDx) > 0.06;
  }

  if (thumbOpen) fingers += 1;
  return fingers;
}

function getHandedness(results) {
  const raw = results?.multiHandedness?.[0]?.label;
  if (raw === "Left" || raw === "Right") return raw;
  return null;
}

function lowPassFilter(alpha, prev, next) {
  return prev == null ? next : prev * (1 - alpha) + next * alpha;
}

export function useAiCamera() {
  const videoRef = useRef(null);

  const [wsConnected, setWsConnected] = useState(false);
  const [sessionActive, setSessionActive] = useState(false);
  const [sessionDeviceId, setSessionDeviceId] = useState(null);
  const [isCameraRunning, setIsCameraRunning] = useState(false);
  const [isSendingAngles, setIsSendingAngles] = useState(false);
  const [angles, setAngles] = useState(() => [0, 0, 0, 0, 0, 0]);
  const [error, setError] = useState("");
  const [selectedJoint, setSelectedJoint] = useState(0);
  const [fingersCount, setFingersCount] = useState(0);

  const role = useMemo(() => String(localStorage.getItem("role") || "").toUpperCase(), []);
  const isViewer = role === "VIEWER";

  const wsConnectedRef = useRef(false);
  const sessionActiveRef = useRef(false);
  const sessionDeviceIdRef = useRef(null);
  const runningRef = useRef(false);
  const isSendingAnglesRef = useRef(false);

  const debugRef = useRef({
    landmarks: null,
    handednessRaw: null,
    palmX: null, // raw filtered (0..1) in video coords
    control: 0,
  });

  const anglesRef = useRef([0, 0, 0, 0, 0, 0]);
  const lastTickRef = useRef(0);
  const lastSendRef = useRef(0);
  const lastRestSendRef = useRef(0);
  const palmXFilteredRef = useRef(null);

  const selectedJointRef = useRef(0);
  const pendingJointRef = useRef(0);
  const pendingSinceRef = useRef(0);
  const lastUiSelectedJointRef = useRef(0);
  const lastUiFingersCountRef = useRef(0);

  const streamRef = useRef(null);
  const handsRef = useRef(null);
  const rafRef = useRef(0);

  const consecutiveSendErrorsRef = useRef(0);
  const sendErrorShownRef = useRef(false);
  const consecutiveWsSendErrorsRef = useRef(0);

  const wsServiceRef = useRef(null);

  const lastAiAnglesConsoleLogRef = useRef(0);

  const SESSION_DEVICE_KEY = "robotSession.deviceId";
  const LAST_SESSION_ID_KEY = "robotLogs.lastSessionId";
  const LAST_TELEMETRY_KEY = "robotTelemetry.last";

  const sessionStartedAtRef = useRef(0);
  const lastFpsRef = useRef(null);

  function getClientDeviceType() {
    const ua = String(navigator.userAgent || "");
    return /Mobi|Android|iPhone|iPad/i.test(ua) ? "Mobile" : "Desktop";
  }

  function getClientDeviceName() {
    // Browsers can't access the OS hostname for privacy reasons.
    // Best-effort: platform + browser UA.
    const platform = navigator.userAgentData?.platform || navigator.platform || "Unknown";
    return String(platform);
  }

  function getClientModel() {
    const ua = String(navigator.userAgent || "");
    return ua;
  }

  function getInternetStatus() {
    const online = navigator.onLine;
    const conn = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
    const effectiveType = conn?.effectiveType;
    if (typeof effectiveType === "string" && effectiveType) {
      return online ? `Online (${effectiveType})` : "Offline";
    }
    return online ? "Online" : "Offline";
  }

  function stopLocal() {
    runningRef.current = false;
    isSendingAnglesRef.current = false;
    setIsSendingAngles(false);
    setIsCameraRunning(false);

    if (rafRef.current) {
      cancelAnimationFrame(rafRef.current);
      rafRef.current = 0;
    }

    if (handsRef.current) {
      try {
        handsRef.current.close();
      } catch {
        // ignore
      }
      handsRef.current = null;
    }

    const stream = streamRef.current;
    if (stream) {
      try {
        stream.getTracks().forEach((t) => t.stop());
      } catch {
        // ignore
      }
      streamRef.current = null;
    }

    const v = videoRef.current;
    if (v) {
      try {
        v.srcObject = null;
      } catch {
        // ignore
      }
    }
  }

  function ensureWs() {
    if (wsServiceRef.current) return wsServiceRef.current;

    const wsUrl = buildWsUrl("/ws/robot-control");
    console.info("[AI Camera] WS URL:", wsUrl);
    const svc = new WebsocketService(wsUrl, {
      reconnect: { enabled: true },
    });

    svc.setHandlers({
      onStatus: (connected) => {
        wsConnectedRef.current = connected;
        setWsConnected(connected);
        console.info("[AI Camera] WS status:", connected ? "connected" : "disconnected");
      },
      onError: (err) => {
        // keep UI minimal; status indicator is enough
        console.warn("[AI Camera] WS error:", err);
      },
      onMessage: (msg) => {
        if (msg && typeof msg === "object") {
          if (msg.type === "camera_control" && String(msg.command).toUpperCase() === "STOP") {
            setSessionActive(false);
            sessionActiveRef.current = false;
            stopLocal();
            sessionDeviceIdRef.current = null;
            setSessionDeviceId(null);
          }
        }
      },
    });

    wsServiceRef.current = svc;
    svc.connect();
    return svc;
  }

  function getPreferredDeviceId() {
    // Prefer session-scoped selected device.
    const fromSession = sessionStorage.getItem(SESSION_DEVICE_KEY);
    if (fromSession != null && String(fromSession).trim() !== "") {
      const n = Number(fromSession);
      if (Number.isFinite(n)) return n;
    }

    // Minimal mechanism (no new UI): allow selecting deviceId via URL (?deviceId=)
    // or existing localStorage keys if the app already stores it.
    try {
      const qs = new URLSearchParams(window.location.search);
      const fromQs = qs.get("deviceId");
      if (fromQs != null && String(fromQs).trim() !== "") return Number(fromQs);
    } catch {
      // ignore
    }

    const fromStorage = localStorage.getItem("deviceId") || localStorage.getItem("selectedDeviceId");
    if (fromStorage != null && String(fromStorage).trim() !== "") return Number(fromStorage);
    return null;
  }

  async function refreshStatus() {
    try {
      const status = await cameraService.status();
      const active = !!status?.sessionActive;
      sessionActiveRef.current = active;
      setSessionActive(active);

      const dev = status?.deviceId ?? null;
      sessionDeviceIdRef.current = dev;
      setSessionDeviceId(dev);
    } catch (e) {
      setError(e?.message || "Failed to fetch camera status");
    }
  }

  async function startSession() {
    if (isViewer) return;
    setError("");

    const deviceId = getPreferredDeviceId();
    if (deviceId == null || Number.isNaN(deviceId)) {
      setError("Please select a device first.");
      return;
    }

    try {
      const started = await cameraService.start(deviceId);
      sessionActiveRef.current = true;
      setSessionActive(true);

      sessionStartedAtRef.current = performance.now();

      const dev = started?.deviceId ?? deviceId;
      sessionDeviceIdRef.current = dev;
      setSessionDeviceId(dev);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to start session");
    }
  }

  async function endSession() {
    if (isViewer) return;
    setError("");

    try {
      const stopped = await cameraService.stop();
      sessionActiveRef.current = false;
      setSessionActive(false);

      const sessionId = stopped?.sessionId ?? null;
      const deviceId = stopped?.deviceId ?? sessionDeviceIdRef.current ?? getPreferredDeviceId();

      const telemetry = {
        timestamp: new Date().toISOString(),
        deviceId: deviceId != null ? Number(deviceId) : null,
        deviceName: getClientDeviceName(),
        deviceType: getClientDeviceType(),
        model: getClientModel(),
        jointData: anglesRef.current?.slice?.(0, 6) || [0, 0, 0, 0, 0, 0],
        battery: null,
        temperatures: { robot: null, motor: null, cpu: null },
        fps: lastFpsRef.current,
        internet: getInternetStatus(),
        uptimeSeconds:
          sessionStartedAtRef.current > 0
            ? (performance.now() - sessionStartedAtRef.current) / 1000
            : null,
        sessionId,
      };

      try {
        sessionStorage.setItem(LAST_TELEMETRY_KEY, JSON.stringify(telemetry));
      } catch {
        // ignore
      }

      if (sessionId != null && deviceId != null && Number.isFinite(Number(deviceId))) {
        try {
          sessionStorage.setItem(LAST_SESSION_ID_KEY, String(sessionId));
        } catch {
          // ignore
        }

        try {
          await logService.ingest({
            robotId: Number(deviceId),
            sessionId,
            userId: stopped?.userId ?? null,
            factoryId: stopped?.factoryId ?? null,
            logType: "AUDIT",
            severity: "INFO",
            source: "CAMERA",
            message: `Session ended (deviceId=${deviceId})`,
            eventTime: new Date().toISOString(),
            metadata: {
              controlMode: stopped?.controlMode ?? "CAMERA",
              telemetry,
            },
          });
        } catch {
          // If log ingestion fails, do not block ending session.
        }
      }

      return stopped;
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to end session");
      return null;
    } finally {
      stopLocal();
      sessionDeviceIdRef.current = null;
      setSessionDeviceId(null);
    }
  }

  async function startWebcam() {
    const v = videoRef.current;
    if (!v) throw new Error("Video element not ready");

    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
    streamRef.current = stream;

    v.srcObject = stream;
    await v.play();

    setIsCameraRunning(true);
  }

  async function startCamera() {
    if (isViewer) return;

    if (!sessionActiveRef.current) {
      setError("Start Session first.");
      return;
    }

    setError("");
    ensureWs();

    try {
      await startWebcam();
    } catch (e) {
      setError(e?.message || "Camera permission denied or webcam unavailable");
      return;
    }

    // reset angles before run
    anglesRef.current = [0, 0, 0, 0, 0, 0];
    setAngles([0, 0, 0, 0, 0, 0]);
    selectedJointRef.current = 0;
    pendingJointRef.current = 0;
    pendingSinceRef.current = performance.now();
    lastUiSelectedJointRef.current = 0;
    lastUiFingersCountRef.current = 0;
    setSelectedJoint(0);
    setFingersCount(0);
    lastTickRef.current = performance.now();
    lastSendRef.current = 0;
    lastRestSendRef.current = 0;
    palmXFilteredRef.current = null;

    consecutiveSendErrorsRef.current = 0;
    sendErrorShownRef.current = false;
    consecutiveWsSendErrorsRef.current = 0;

    runningRef.current = true;
    isSendingAnglesRef.current = true;
    setIsSendingAngles(true);

    initHands();
    await startLoop();
  }

  async function stopCamera() {
    if (isViewer) return;
    setError("");
    stopLocal();
  }

  function initHands() {
    const hands = new Hands({
      locateFile: (file) => {
        const base = import.meta.env.VITE_MEDIAPIPE_ASSETS_BASE_URL;
        if (base) return `${String(base).replace(/\/$/, "")}/${file}`;
        return `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`;
      },
    });

    hands.setOptions({
      maxNumHands: 1,
      modelComplexity: 1,
      selfieMode: false,
      minDetectionConfidence: 0.5,
      minTrackingConfidence: 0.5,
    });

    hands.onResults((results) => {
      if (!runningRef.current) return;
      if (!sessionActiveRef.current) return;

      const now = performance.now();
      const last = lastTickRef.current || now;
      const dt = Math.max(0.001, Math.min(0.2, (now - last) / 1000));
      lastTickRef.current = now;

      lastFpsRef.current = dt > 0 ? 1 / dt : null;

      const landmarks = results?.multiHandLandmarks?.[0];
      if (!landmarks) return;

      debugRef.current.landmarks = landmarks;

      // Python-style: count fingers -> choose joint (0..5), only switch if held for MODE_HOLD_MS
      const handednessRaw = getHandedness(results);
      debugRef.current.handednessRaw = handednessRaw;

      const fingersCount = countFingers(landmarks, handednessRaw);
      const targetJoint = clamp(fingersCount, 0, 5);
      if (targetJoint !== pendingJointRef.current) {
        pendingJointRef.current = targetJoint;
        pendingSinceRef.current = now;
      } else {
        if (now - pendingSinceRef.current >= MODE_HOLD_MS) {
          selectedJointRef.current = pendingJointRef.current;
        }
      }

      if (lastUiFingersCountRef.current !== fingersCount) {
        lastUiFingersCountRef.current = fingersCount;
        setFingersCount(fingersCount);
      }
      if (lastUiSelectedJointRef.current !== selectedJointRef.current) {
        lastUiSelectedJointRef.current = selectedJointRef.current;
        setSelectedJoint(selectedJointRef.current);
      }

      const palmXRaw = computePalmCenterX(landmarks);
      if (palmXRaw == null) return;

      // low-pass filter to reduce jitter
      const alpha = 0.25; // hoặc nhỏ hơn để mượt hơn, ví dụ 0.15
      const prev = palmXFilteredRef.current;
      const filteredRaw = lowPassFilter(alpha, prev, palmXRaw);
      palmXFilteredRef.current = filteredRaw;

      debugRef.current.palmX = filteredRaw;

      const filteredForControl = maybeMirror01(filteredRaw);
      if (filteredForControl == null) return;

      const control = normalizePalmX(filteredForControl);
      debugRef.current.control = control;

      // Python-style: integrate ONLY the selected joint
      const next = anglesRef.current.slice(0, 6);
      const j = clamp(selectedJointRef.current, 0, 5);
      const [minL, maxL] = JOINT_LIMITS[j];
      const speed = SPEEDS_DEG_PER_SEC[j];
      next[j] = clamp(next[j] + control * speed * dt, minL, maxL);

      anglesRef.current = next;

      // send at ~20 msg/s
      const sendIntervalMs = 50;
      if (!isSendingAnglesRef.current) return;
      if (now - lastSendRef.current < sendIntervalMs) return;
      lastSendRef.current = now;

      const deviceId = sessionDeviceIdRef.current ?? getPreferredDeviceId();
      if (deviceId == null || Number.isNaN(deviceId)) return;
      const payloadAngles = next.map((n) => Number(n));
      const payload = { type: "ai_angles", deviceId, angles: payloadAngles };

      // Preferred path: REST -> BE will forward/broadcast over WS to Unity.
      // Keep it slower to reduce backend load.
      const restMinIntervalMs = 100;
      if (now - lastRestSendRef.current >= restMinIntervalMs) {
        lastRestSendRef.current = now;
        cameraService.sendAngles(payloadAngles, deviceId).catch(() => {
          // keep UI minimal; status pills indicate connectivity
        });
      }

      // Optional: WS direct (useful for debugging), but do not depend on it.
      if (wsConnectedRef.current) {
        const svc = wsServiceRef.current;
        const ok = svc?.sendJson(payload);
        if (!ok) {
          consecutiveWsSendErrorsRef.current += 1;
        } else {
          consecutiveWsSendErrorsRef.current = 0;
        }
      }

      // Throttled diagnostics (once/sec)
      if (now - lastAiAnglesConsoleLogRef.current >= 1000) {
        lastAiAnglesConsoleLogRef.current = now;
        console.debug("[AI Camera] send ai_angles:", payload);
      }

      setAngles(next);
    });

    handsRef.current = hands;
  }

  async function startLoop() {
    const v = videoRef.current;
    const hands = handsRef.current;
    if (!v || !hands) return;

    async function frame() {
      if (!runningRef.current) return;
      try {
        await hands.send({ image: v });
        consecutiveSendErrorsRef.current = 0;
      } catch (e) {
        consecutiveSendErrorsRef.current += 1;
        if (consecutiveSendErrorsRef.current >= 10 && !sendErrorShownRef.current) {
          sendErrorShownRef.current = true;
          setError(
            "Hand tracking failed to run. Possible cause: MediaPipe assets blocked/unreachable (CDN) or insecure context. " +
              "Open DevTools Console/Network for details. " +
              (e?.message ? `(${e.message})` : "")
          );
        }
      }
      rafRef.current = requestAnimationFrame(frame);
    }

    rafRef.current = requestAnimationFrame(frame);
  }

  async function start() {
    // Backward compatibility: Start acts like Start Session + Start AI Camera.
    await startSession();
    if (!sessionActiveRef.current) return;
    await startCamera();
  }

  async function stop() {
    // Backward compatibility: Stop acts like End Session.
    await endSession();
  }

  useEffect(() => {
    // bootstrap without calling setState directly inside the effect body
    const bootstrap = () => {
      ensureWs();
      refreshStatus();
    };

    bootstrap();

    return () => {
      stopLocal();
      wsServiceRef.current?.disconnect();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return {
    videoRef,
    role,
    isViewer,
    wsConnected,
    sessionActive,
    sessionDeviceId,
    isCameraRunning,
    isSendingAngles,
    angles,
    selectedJoint,
    fingersCount,
    selfieMode: SELFIE_MODE,
    deadzone: DEADZONE,
    maxOffset: MAX_OFFSET,
    debugRef,
    error,
    startSession,
    endSession,
    startCamera,
    stopCamera,
    start,
    stop,
    refreshStatus,
  };
}
