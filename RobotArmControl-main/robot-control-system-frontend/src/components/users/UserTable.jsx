import React from "react";
import { getFactories } from "../../api/factoryService";

export default function UserTable({ users, loading, onEdit, onDelete }) {
  const [factories, setFactories] = React.useState({});
  const showActions = typeof onEdit === "function" || typeof onDelete === "function";

  React.useEffect(() => {
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

  if (loading) {
    return (
      <div className="px-5 py-8 text-center text-white/60">Loading...</div>
    );
  }

  if (!users || users.length === 0) {
    return (
      <div className="px-5 py-8 text-center text-white/60">No users found</div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="border-b border-white/10 text-left text-xs uppercase tracking-wider text-white/50">
            <th className="px-5 py-3 font-medium">ID</th>
            <th className="px-5 py-3 font-medium">Username</th>
            <th className="px-5 py-3 font-medium">Email</th>
            <th className="px-5 py-3 font-medium">Role</th>
            <th className="px-5 py-3 font-medium">Factory</th>
            <th className="px-5 py-3 font-medium">Status</th>
            <th className="px-5 py-3 font-medium">Created</th>
            {showActions && <th className="px-5 py-3 font-medium">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr
              key={user.userId}
              className="border-b border-white/5 hover:bg-white/5 transition"
            >
              <td className="px-5 py-4">
                <div className="text-sm text-white/50 font-mono">
                  {user.userId}
                </div>
              </td>
              <td className="px-5 py-4">
                <div className="text-sm text-white font-medium">
                  {user.username}
                </div>
              </td>
              <td className="px-5 py-4">
                <div className="text-sm text-white/60">
                  {user.email}
                </div>
              </td>
              <td className="px-5 py-4">
                <span className="inline-block px-2 py-1 rounded text-xs font-medium bg-blue-500/20 text-blue-300">
                  {user.role || "Unknown"}
                </span>
              </td>
              <td className="px-5 py-4">
                <div className="text-sm text-white/60">
                  {factories[user.factoryId] || "Unknown"}
                </div>
              </td>
              <td className="px-5 py-4">
                {(() => {
                  const s = String(user.status || "").toLowerCase();
                  const isActive = s === "active";
                  const cls = isActive ? "bg-green-500/20 text-green-300" : "bg-red-500/20 text-red-300";
                  const label = s ? s.toUpperCase() : "UNKNOWN";
                  return (
                    <span className={`inline-block px-2 py-1 rounded text-xs font-medium ${cls}`}>{label}</span>
                  );
                })()}
              </td>
              <td className="px-5 py-4">
                <div className="text-sm text-white/60">
                  {user.createdAt
                    ? new Date(user.createdAt).toLocaleDateString()
                    : "-"}
                </div>
              </td>
              {showActions && (
                <td className="px-5 py-4">
                  <div className="flex gap-2">
                    {typeof onEdit === "function" && (
                      <button
                        onClick={() => onEdit(user)}
                        className="px-3 py-1 rounded text-xs bg-blue-500/20 text-blue-300 hover:bg-blue-500/30 transition"
                      >
                        Edit
                      </button>
                    )}
                    {typeof onDelete === "function" && (
                      <button
                        onClick={() => onDelete(user)}
                        className="px-3 py-1 rounded text-xs bg-red-500/20 text-red-300 hover:bg-red-500/30 transition"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
