import React, { useEffect, useState } from "react";

export default function CreateDeviceModal({ open, onClose, onSubmit, loading, hubs, initialHubId }) {
  const [form, setForm] = useState({
    deviceName: "", hubId: "", deviceType: "RobotArm", robotType: "Unity",
    model: "", serialNumber: "", connectionType: "USB",
  });
  const [error, setError] = useState("");

  useEffect(() => {
    if (!open) return;
    if (!initialHubId) return;
    setForm((p) => (p.hubId ? p : { ...p, hubId: String(initialHubId) }));
  }, [open, initialHubId]);

  if (!open) return null;

  function handleSubmit() {
    if (!form.deviceName.trim()) { setError("Name is required."); return; }
    if (!form.hubId) { setError("Please select a hub."); return; }
    setError("");
    onSubmit({
      hubId: Number(form.hubId),
      deviceName: form.deviceName.trim(),
      deviceType: form.deviceType,
      robotType: form.robotType,
      model: form.model.trim(),
      serialNumber: form.serialNumber.trim(),
      connectionType: form.connectionType,
    });
    setForm({ deviceName: "", hubId: "", deviceType: "RobotArm", robotType: "Unity", model: "", serialNumber: "", connectionType: "USB" });
  }

  function handleClose() {
    setError("");
    setForm({ deviceName: "", hubId: "", deviceType: "RobotArm", robotType: "Unity", model: "", serialNumber: "", connectionType: "USB" });
    onClose();
  }

  const field = (label, key, placeholder) => (
    <label className="block">
      <div className="text-xs text-white/60 mb-1">{label}</div>
      <input className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
        value={form[key]} onChange={(e) => setForm((p) => ({ ...p, [key]: e.target.value }))} placeholder={placeholder} />
    </label>
  );

  const select = (label, key, options) => (
    <label className="block">
      <div className="text-xs text-white/60 mb-1">{label}</div>
      <select className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
        value={form[key]} onChange={(e) => setForm((p) => ({ ...p, [key]: e.target.value }))}>
        {options.map((o) => <option key={o} className="bg-neutral-900" value={o}>{o}</option>)}
      </select>
    </label>
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/10 bg-neutral-950 p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-white font-semibold">Create Device</div>
            <div className="text-sm text-white/60 mt-1">Enter device information</div>
          </div>
          <button type="button" className="text-white/60 hover:text-white" onClick={handleClose} disabled={loading}>✕</button>
        </div>

        {error && <div className="mt-3 text-sm text-red-300">{error}</div>}

        <div className="mt-4 space-y-3">
          {field("Device Name", "deviceName", "e.g. Robot Arm #1")}
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Hub</div>
            <select className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.hubId} onChange={(e) => setForm((p) => ({ ...p, hubId: e.target.value }))}>
              <option className="bg-neutral-900" value="">Select a hub...</option>
              {(hubs || []).map((h) => (
                <option key={h.hubId} className="bg-neutral-900" value={h.hubId}>{h.hubName}</option>
              ))}
            </select>
          </label>
          {select("Device Type", "deviceType", ["RobotArm"])}
          {select("Robot Type", "robotType", ["Unity", "Real"])}
          {field("Model", "model", "e.g. UR5e")}
          {field("Serial Number", "serialNumber", "e.g. SN-12345")}
          {select("Connection Type", "connectionType", ["USB", "Serial", "TCP"])}
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button type="button" className="h-10 px-4 rounded-lg bg-white/10 text-white hover:bg-white/15 transition disabled:opacity-60" onClick={handleClose} disabled={loading}>Cancel</button>
          <button type="button" className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60" onClick={handleSubmit} disabled={loading}>
            {loading ? "Creating..." : "Create"}
          </button>
        </div>
      </div>
    </div>
  );
}