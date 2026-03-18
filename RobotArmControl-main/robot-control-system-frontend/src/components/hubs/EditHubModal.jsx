import React, { useState } from "react";

export default function EditHubModal({ open, hub, onClose, onSubmit, loading, areas }) {
  const [form, setForm] = useState(() => ({
    hubName: hub?.hubName || "",
    hubDescription: hub?.hubDescription || "",
    areaId: "",
    status: "Active",
  }));
  const [error, setError] = useState("");

  if (!open || !hub) return null;

  function handleSubmit() {
    const name = form.hubName.trim();
    if (!name) { setError("Name is required."); return; }
    if (!form.areaId) { setError("Please select an area."); return; }
    setError("");
    onSubmit(hub.hubId, { hubName: form.hubName, hubDescription: form.hubDescription, areaId: Number(form.areaId), status: form.status });
  }

  function handleClose() { setError(""); onClose(); }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/10 bg-neutral-950 p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-white font-semibold">Edit Hub</div>
            <div className="text-sm text-white/60 mt-1">Update hub information</div>
          </div>
          <button type="button" className="text-white/60 hover:text-white" onClick={handleClose} disabled={loading}>✕</button>
        </div>

        {error && <div className="mt-3 text-sm text-red-300">{error}</div>}

        <div className="mt-4 space-y-3">
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Name</div>
            <input className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.hubName} onChange={(e) => setForm((p) => ({ ...p, hubName: e.target.value }))} />
          </label>
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Description</div>
            <input className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.hubDescription} onChange={(e) => setForm((p) => ({ ...p, hubDescription: e.target.value }))} />
          </label>
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Area</div>
            <select className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.areaId} onChange={(e) => setForm((p) => ({ ...p, areaId: e.target.value }))}>
              <option className="bg-neutral-900" value="">Select an area...</option>
              {(areas || []).map((a) => (
                <option key={a.areaId} className="bg-neutral-900" value={a.areaId}>{a.areaName}</option>
              ))}
            </select>
          </label>
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Status</div>
            <select className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.status} onChange={(e) => setForm((p) => ({ ...p, status: e.target.value }))}>
              <option className="bg-neutral-900" value="Active">Active</option>
              <option className="bg-neutral-900" value="Inactive">Inactive</option>
            </select>
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button type="button" className="h-10 px-4 rounded-lg bg-white/10 text-white hover:bg-white/15 transition disabled:opacity-60" onClick={handleClose} disabled={loading}>Cancel</button>
          <button type="button" className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60" onClick={handleSubmit} disabled={loading}>
            {loading ? "Saving..." : "Save Changes"}
          </button>
        </div>
      </div>
    </div>
  );
}
