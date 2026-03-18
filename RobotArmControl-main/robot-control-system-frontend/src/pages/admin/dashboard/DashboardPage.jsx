import React, { useEffect, useMemo, useState } from "react";
import { getRole, isOperatorRole } from "../../../utils/auth";
import { logService } from "../../../api/logService";

function StatusBadge({ status }) {
  const s = String(status || "").toLowerCase();
  const cls =
    s === "success"
      ? "bg-green-500/15 text-green-300"
      : s === "queued"
        ? "bg-white/10 text-white/70"
        : "bg-red-500/15 text-red-300";

  return (
    <span
      className={[
        "inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium",
        cls,
      ].join(" ")}
    >
      {s || "unknown"}
    </span>
  );
}

export default function DashboardPage() {
  const isOperator = isOperatorRole(getRole());

  const [loadingLogs, setLoadingLogs] = useState(false);
  const [logsError, setLogsError] = useState("");
  const [sessionLogs, setSessionLogs] = useState([]);

  const lastSessionId = useMemo(() => {
    const raw = sessionStorage.getItem("robotLogs.lastSessionId");
    if (!raw) return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  }, []);

  const lastTelemetry = useMemo(() => {
    const raw = sessionStorage.getItem("robotTelemetry.last");
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }, []);

  const stats = [
    { label: "Factories", value: 5 },
    { label: "Areas", value: 12 },
    { label: "Hubs", value: 9 },
    { label: "Devices", value: 28 },
  ];

  useEffect(() => {
    if (!isOperator) return;
    if (lastSessionId == null) return;

    let mounted = true;
    setLoadingLogs(true);
    setLogsError("");

    logService
      .getSessionLogs(lastSessionId, { limit: 100 })
      .then((data) => {
        if (!mounted) return;
        setSessionLogs(Array.isArray(data) ? data : []);
      })
      .catch((e) => {
        if (!mounted) return;
        setLogsError(e?.response?.data?.message || e?.message || "Failed to load logs");
        setSessionLogs([]);
      })
      .finally(() => {
        if (!mounted) return;
        setLoadingLogs(false);
      });

    return () => {
      mounted = false;
    };
  }, [isOperator, lastSessionId]);

  const logs = useMemo(() => {
    if (!isOperator) {
      return [
        {
          time: "2026-02-02 09:21",
          actor: "Admin",
          action: "Created device",
          target: "Robot Arm #10",
          status: "success",
        },
        {
          time: "2026-02-02 09:05",
          actor: "Admin",
          action: "Created hub",
          target: "Hub-01 (Area A)",
          status: "success",
        },
        {
          time: "2026-02-01 16:48",
          actor: "Admin",
          action: "Sent command",
          target: "Robot Arm #07",
          status: "queued",
        },
      ];
    }

    const allowedTypes = new Set(["AUDIT", "COMMAND", "AI_GESTURE"]);

    const filtered = (sessionLogs || []).filter((l) => {
      const logType = String(l?.logType || "").toUpperCase();
      const message = String(l?.message || "");

      if (logType === "TELEMETRY") return false;
      if (/^\s*periodic telemetry update\s*$/i.test(message)) return false;

      if (!allowedTypes.has(logType)) return false;
      return true;
    });

    return filtered.map((l) => {
      const time = l?.eventTime ? new Date(l.eventTime).toLocaleString() : "—";
      const actor = l?.userId != null ? `User #${l.userId}` : "—";
      const action = l?.logType ? String(l.logType) : "LOG";
      const target = l?.robotId != null ? `Robot #${l.robotId}` : "—";
      const status = l?.severity ? String(l.severity).toLowerCase() : "info";

      return {
        time,
        actor,
        action: `${action} — ${l?.message || ""}`.trim(),
        target,
        status,
        _key: String(l?.eventId || `${time}-${action}-${target}`),
      };
    });
  }, [isOperator, sessionLogs]);

  return (
    <div className="w-full">
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-white text-left align-top">{isOperator ? "Logs" : "Dashboard"}</h1>
        <p className="mt-1 text-sm text-white/60 text-left align-top">
          {isOperator ? "Recent activity and system logs" : "Overview of your RoboArm system"}
        </p>
      </div>

      {!isOperator && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {stats.map((s) => (
            <div
              key={s.label}
              className="rounded-2xl border border-white/10 bg-neutral-950/40 p-5"
            >
              <div className="text-xs uppercase tracking-wider text-white/50">
                {s.label}
              </div>
              <div className="mt-2 text-3xl font-semibold text-white">
                {s.value}
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-4 ">
        <div className="lg:col-span-2  rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden ">
          <div className="px-5 py-4 border-b border-white/10 ">
            <div className="text-xs uppercase tracking-wider text-white/50">
              Activity
            </div>
          </div>

          {isOperator && lastTelemetry && (
            <div className="px-5 py-4 border-b border-white/10">
              <div className="text-lg font-semibold text-white text-left">TELEMETRY Details</div>
              <div className="mt-3 space-y-1 text-sm text-white/70">
                <div>Timestamp: {lastTelemetry.timestamp || "—"}</div>
                <div>DeviceId: {lastTelemetry.deviceId ?? "—"}</div>
                <div>DeviceName: {lastTelemetry.deviceName || "—"}</div>
                <div>DeviceType: {lastTelemetry.deviceType || "—"}</div>
                <div>Model: {lastTelemetry.model || "—"}</div>
                <div>
                  JointData: {Array.isArray(lastTelemetry.jointData)
                    ? lastTelemetry.jointData.map((n) => Number(n).toFixed(3)).join(", ")
                    : "—"}
                </div>
                <div>
                  Battery: {lastTelemetry.battery != null ? String(lastTelemetry.battery) : "—"}
                </div>
                <div>
                  Temperatures: {lastTelemetry.temperatures
                    ? `robot=${lastTelemetry.temperatures.robot ?? "—"}, motor=${lastTelemetry.temperatures.motor ?? "—"}, cpu=${lastTelemetry.temperatures.cpu ?? "—"}`
                    : "—"}
                </div>
                <div>FPS: {lastTelemetry.fps != null ? Number(lastTelemetry.fps).toFixed(2) : "—"}</div>
                <div>Internet: {lastTelemetry.internet || "—"}</div>
                <div>
                  UptimeSeconds: {lastTelemetry.uptimeSeconds != null ? Number(lastTelemetry.uptimeSeconds).toFixed(2) : "—"}
                </div>
              </div>
            </div>
          )}

            {isOperator && lastSessionId == null && (
              <div className="px-5 py-4 text-sm text-white/50">
                No session logs yet. End a session to generate a log entry.
              </div>
            )}

            {isOperator && logsError && (
              <div className="px-5 py-4 text-sm text-red-300">{logsError}</div>
            )}

            {isOperator && loadingLogs && (
              <div className="px-5 py-4 text-sm text-white/50">Loading logs...</div>
            )}

            <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-xs uppercase tracking-wider text-white/40">
                  <th className="text-left font-medium px-5 py-3">Time</th>
                  <th className="text-left font-medium px-5 py-3">Actor</th>
                  <th className="text-left font-medium px-5 py-3">Action</th>
                  <th className="text-left font-medium px-5 py-3">Target</th>
                  <th className="text-left font-medium px-5 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/10">
                {logs.map((l) => (
                  <tr
                    key={l._key || l.time + l.action}
                    className="hover:bg-white/5 transition"
                  >
                    <td className="px-5 py-4 text-white/70">{l.time}</td>
                    <td className="px-5 py-4 text-white/70">{l.actor}</td>
                    <td className="px-5 py-4 text-white">{l.action}</td>
                    <td className="px-5 py-4 text-white/70">{l.target}</td>
                    <td className="px-5 py-4">
                      <StatusBadge status={l.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

       
      </div>
    </div>
  );
}
