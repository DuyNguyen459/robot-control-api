import React from "react";

function StatusPill({ active }) {
  return (
    <span
      className={[
        "inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium",
        active ? "bg-green-500/15 text-green-300" : "bg-white/10 text-white/60",
      ].join(" ")}
    >
      {active ? "Active" : "Inactive"}
    </span>
  );
}

export default function HubTable({ hubs, loading, onEdit, onDelete, onRowClick }) {
  const showActions = typeof onEdit === "function" || typeof onDelete === "function";

  if (loading) {
    return <div className="px-5 py-10 text-center text-white/50 text-sm">Loading hubs...</div>;
  }

  if (!hubs || hubs.length === 0) {
    return <div className="px-5 py-10 text-center text-white/50 text-sm">No hubs found.</div>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-xs uppercase tracking-wider text-white/40">
            <th className="text-left font-medium px-5 py-3">Name</th>
            <th className="text-left font-medium px-5 py-3">Area</th>
            <th className="text-left font-medium px-5 py-3">Description</th>
            <th className="text-left font-medium px-5 py-3">Status</th>
            {showActions && <th className="text-left font-medium px-5 py-3">Actions</th>}
          </tr>
        </thead>
        <tbody className="divide-y divide-white/10">
          {hubs.map((h) => {
            const active = String(h.hubStatus).toLowerCase() === "active";
            const clickable = typeof onRowClick === "function";
            return (
              <tr
                key={h.hubId}
                className={["hover:bg-white/5 transition", clickable ? "cursor-pointer" : ""].join(" ")}
                onClick={clickable ? () => onRowClick(h) : undefined}
              >
                <td className="px-5 py-4 text-left align-top">
                  <div className="text-white font-medium">{h.hubName}</div>
                  <div className="text-xs text-white/50">ID: {h.hubId}</div>
                </td>
                <td className="px-5 py-4 text-white/70 text-left align-top">{h.areaName || `Area #${h.areaId}`}</td>
                <td className="px-5 py-4 text-white/70 text-left align-top">{h.hubDescription || "No description available"}</td>
                <td className="px-5 py-4 text-left align-top"><StatusPill active={active} /></td>
                {showActions && (
                  <td className="px-5 py-4 text-left align-top">
                    <div className="flex items-center gap-2">
                      {typeof onEdit === "function" && (
                        <button type="button" onClick={(e) => { e.stopPropagation(); onEdit(h); }} className="px-3 py-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-white/70 hover:text-white text-xs transition">Edit</button>
                      )}
                      {typeof onDelete === "function" && (
                        <button type="button" onClick={(e) => { e.stopPropagation(); onDelete(h); }} className="px-3 py-1.5 rounded-lg bg-red-500/10 hover:bg-red-500/20 text-red-300 text-xs transition">Delete</button>
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
