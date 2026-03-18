import React from "react";

function StatusPill({ status }) {
  const s = String(status || "").toLowerCase();
  const cls = s === "active" ? "bg-green-500/15 text-green-300"
    : s === "fault" ? "bg-red-500/15 text-red-300"
    : "bg-white/10 text-white/60";
  const label = s === "active" ? "Active" : s === "fault" ? "Fault" : "Not Active";
  return <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${cls}`}>{label}</span>;
}

export default function DeviceTable({ devices, loading, onEdit, onDelete, onRowClick }) {
  const showActions = typeof onEdit === "function" || typeof onDelete === "function";

  if (loading) return <div className="px-5 py-10 text-center text-white/50 text-sm">Loading devices...</div>;
  if (!devices || devices.length === 0) return <div className="px-5 py-10 text-center text-white/50 text-sm">No devices found.</div>;

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-xs uppercase tracking-wider text-white/40">
            <th className="text-left font-medium px-5 py-3">Name</th>
            <th className="text-left font-medium px-5 py-3">Device Type</th>
            <th className="text-left font-medium px-5 py-3">Robot Type</th>
            <th className="text-left font-medium px-5 py-3">Model</th>
            <th className="text-left font-medium px-5 py-3">Connection</th>
            <th className="text-left font-medium px-5 py-3">Hub</th>
            <th className="text-left font-medium px-5 py-3">Status</th>
            {showActions && <th className="text-left font-medium px-5 py-3">Actions</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-white/10">
          {devices.map((d) => {
            const clickable = typeof onRowClick === "function";
            return (
            <tr
              key={d.deviceId}
              className={["hover:bg-white/5 transition", clickable ? "cursor-pointer" : ""].join(" ")}
              onClick={clickable ? () => onRowClick(d) : undefined}
            >
              <td className="px-5 py-4 text-left align-top">
                <div className="text-white font-medium">{d.deviceName}</div>
                <div className="text-xs text-white/50">SN: {d.serialNumber || "—"}</div>
              </td>
              <td className="px-5 py-4 text-white/70 text-left align-top">{d.deviceType || "—"}</td>
              <td className="px-5 py-4 text-white/70 text-left align-top">{d.robotType || "—"}</td>
              <td className="px-5 py-4 text-white/70 text-left align-top">{d.model || "—"}</td>
              <td className="px-5 py-4 text-white/70 text-left align-top">{d.connectionType || "—"}</td>
              <td className="px-5 py-4 text-white/70 text-left align-top">{d.hubName || `Hub #${d.hubId}`}</td>
              <td className="px-5 py-4 text-left align-top"><StatusPill status={d.deviceStatus} /></td>
              {showActions && (
                <td className="px-5 py-4 text-left align-top">
                  <div className="flex items-center gap-2">
                    {typeof onEdit === "function" && (
                      <button type="button" onClick={(e) => { e.stopPropagation(); onEdit(d); }} className="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-white/70 hover:text-white text-xs transition">Edit</button>
                    )}
                    {typeof onDelete === "function" && (
                      <button type="button" onClick={(e) => { e.stopPropagation(); onDelete(d); }} className="px-3 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-300 text-xs transition">Delete</button>
                    )}
                  </div>
                </td>
              )}
            </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}