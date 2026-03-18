import React, { useState } from "react";

export default function EditAreaModal({ open, area, onClose, onSubmit, loading }) {
  const [form, setForm] = useState(() => ({
    areaName: area?.areaName || "",
    areaDescription: area?.areaDescription || "",
  }));
  const [error, setError] = useState("");

  if (!open || !area) return null;

  function handleSubmit() {
    const areaName = form.areaName.trim();
    if (!areaName) { setError("Name is required."); return; }
    setError("");
    onSubmit(area.areaId, { areaName, areaDescription: form.areaDescription.trim() });
  }

  function handleClose() { setError(""); onClose(); }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/10 bg-neutral-950 p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-white font-semibold">Edit Area</div>
            <div className="text-sm text-white/60 mt-1">Update area information</div>
          </div>
          <button type="button" className="text-white/60 hover:text-white" onClick={handleClose} disabled={loading}>✕</button>
        </div>

        {error && <div className="mt-3 text-sm text-red-300">{error}</div>}

        <div className="mt-4 space-y-3">
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Name</div>
            <input className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.areaName} onChange={(e) => setForm((p) => ({ ...p, areaName: e.target.value }))} />
          </label>
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Description</div>
            <input className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20" value={form.areaDescription} onChange={(e) => setForm((p) => ({ ...p, areaDescription: e.target.value }))} />
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