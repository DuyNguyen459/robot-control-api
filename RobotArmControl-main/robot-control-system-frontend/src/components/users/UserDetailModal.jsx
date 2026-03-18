import React, { useState, useEffect } from "react";
import { getFactories } from "../../api/factoryService";

export default function UserDetailModal({ open, onClose, user }) {
  const [factories, setFactories] = useState({});

  useEffect(() => {
    loadFactories();
  }, []);

  async function loadFactories() {
    try {
      const data = await getFactories();
      const map = {};
      (Array.isArray(data) ? data : []).forEach((f) => {
        map[f.factoryId] = f.factoryName;
      });
      setFactories(map);
    } catch (e) {
      console.error("Failed to load factories:", e);
    }
  }

  if (!open || !user) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-2xl border border-white/10 bg-neutral-950 p-6">
        <div className="flex items-start justify-between gap-3 mb-6">
          <div>
            <div className="text-white font-semibold text-lg">User Details</div>
            <div className="text-sm text-white/60 mt-1">View user information</div>
          </div>
          <button
            type="button"
            className="text-white/60 hover:text-white text-xl"
            onClick={onClose}
          >
            ✕
          </button>
        </div>

        <div className="space-y-4">
          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">ID</span>
            <span className="text-white font-mono font-bold">{user.userId}</span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Username</span>
            <span className="text-white font-medium">{user.username}</span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Email</span>
            <span className="text-white text-sm break-all">{user.email}</span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Role</span>
            <span className="inline-block px-2 py-1 rounded text-xs font-medium bg-blue-500/20 text-blue-300">
              {user.role || "Unknown"}
            </span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Factory</span>
            <span className="text-white text-sm">{factories[user.factoryId] || "Unknown"}</span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Status</span>
            <span
              className={`inline-block px-2 py-1 rounded text-xs font-medium ${
                user.status === "Active"
                  ? "bg-green-500/20 text-green-300"
                  : "bg-red-500/20 text-red-300"
              }`}
            >
              {user.status || "Unknown"}
            </span>
          </div>

          <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
            <span className="text-xs uppercase text-white/50 font-semibold">Created At</span>
            <span className="text-white text-sm">
              {user.createdAt ? new Date(user.createdAt).toLocaleString() : "-"}
            </span>
          </div>

          {user.updatedAt && (
            <div className="flex justify-between items-center p-3 bg-white/5 rounded-lg border border-white/10">
              <span className="text-xs uppercase text-white/50 font-semibold">Updated At</span>
              <span className="text-white text-sm">{new Date(user.updatedAt).toLocaleString()}</span>
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={onClose}
          className="w-full mt-6 h-10 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition"
        >
          Close
        </button>
      </div>
    </div>
  );
}
