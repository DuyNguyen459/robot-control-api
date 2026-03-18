import React, { useState, useEffect } from "react";
import { getFactories } from "../../api/factoryService";

export default function CreateUserModal({ open, onClose, onSubmit, loading }) {
  const [form, setForm] = useState({ username: "", email: "", password: "", factoryId: "", status: "Active", role: "OPERATOR" });
  const [error, setError] = useState("");
  const [factories, setFactories] = useState([]);
  const [loadingFactories, setLoadingFactories] = useState(false);

  useEffect(() => {
    if (open) {
      loadFactories();
    }
  }, [open]);

  async function loadFactories() {
    try {
      setLoadingFactories(true);
      const data = await getFactories();
      setFactories(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error("Failed to load factories:", e);
    } finally {
      setLoadingFactories(false);
    }
  }

  if (!open) return null;

  function handleSubmit() {
    const username = form.username.trim();
    const email = form.email.trim();
    const password = form.password.trim();
    const factoryId = form.factoryId;

    if (!username || !email || !password) {
      setError("Username, Email, and Password are required.");
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters long.");
      return;
    }

    if (!email.includes("@")) {
      setError("Please enter a valid email address.");
      return;
    }

    if (!factoryId) {
      setError("Factory is required.");
      return;
    }

    setError("");
    onSubmit({ username, email, password, factoryId, status: form.status, role: form.role });
    setForm({ username: "", email: "", password: "", factoryId: "", status: "Active", role: "OPERATOR" });
  }

  function handleClose() {
    setError("");
    setForm({ username: "", email: "", password: "", factoryId: "", status: "Active", role: "OPERATOR" });
    onClose();
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/10 bg-neutral-950 p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-white font-semibold">Create User Account</div>
            <div className="text-sm text-white/60 mt-1">Enter user information</div>
          </div>
          <button
            type="button"
            className="text-white/60 hover:text-white"
            onClick={handleClose}
            disabled={loading}
          >
            ✕
          </button>
        </div>

        {error && <div className="mt-3 text-sm text-red-300">{error}</div>}

        <div className="mt-4 space-y-3">
          <label className="block">
            <div className="text-xs text-white/60 mb-1">Username</div>
            <input
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.username}
              onChange={(e) =>
                setForm((p) => ({ ...p, username: e.target.value }))
              }
              placeholder="e.g. john.doe"
              disabled={loading}
            />
          </label>

          <label className="block">
            <div className="text-xs text-white/60 mb-1">Email</div>
            <input
              type="email"
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.email}
              onChange={(e) =>
                setForm((p) => ({ ...p, email: e.target.value }))
              }
              placeholder="e.g. john@example.com"
              disabled={loading}
            />
          </label>

          <label className="block">
            <div className="text-xs text-white/60 mb-1">Password</div>
            <input
              type="password"
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.password}
              onChange={(e) =>
                setForm((p) => ({ ...p, password: e.target.value }))
              }
              placeholder="Enter password"
              disabled={loading}
            />
          </label>

          <label className="block">
            <div className="text-xs text-white/60 mb-1">Factory</div>
            <select
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.factoryId}
              onChange={(e) =>
                setForm((p) => ({ ...p, factoryId: e.target.value }))
              }
              disabled={loading || loadingFactories}
            >
              <option value="">Select a factory</option>
              {factories.map((factory) => (
                <option key={factory.factoryId} value={factory.factoryId} className="bg-neutral-900">
                  {factory.factoryName}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <div className="text-xs text-white/60 mb-1">Role</div>
            <select
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.role}
              onChange={(e) =>
                setForm((p) => ({ ...p, role: e.target.value }))
              }
              disabled={loading}
            >
              <option className="bg-neutral-900" value="OPERATOR">Operator</option>
              <option className="bg-neutral-900" value="VIEWER">Viewer</option>
            </select>
          </label>

          <label className="block">
            <select
              className="w-full rounded-lg bg-white/5 border border-white/10 px-3 py-2 text-white outline-none focus:border-white/20"
              value={form.status}
              onChange={(e) =>
                setForm((p) => ({ ...p, status: e.target.value }))
              }
              disabled={loading}
            >
              <option className="bg-neutral-900" value="Active">Active</option>
              <option className="bg-neutral-900" value="Inactive">Inactive</option>
            </select>
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            className="h-10 px-4 rounded-lg bg-white/10 text-white hover:bg-white/15 transition disabled:opacity-60"
            onClick={handleClose}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="button"
            className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            onClick={handleSubmit}
            disabled={loading || loadingFactories}
          >
            {loading ? "Creating..." : "Create"}
          </button>
        </div>
      </div>
    </div>
  );
}
