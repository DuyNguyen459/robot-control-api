import React, { useMemo, useState } from "react";

const makeId = () => `f_${Date.now()}_${Math.floor(Math.random() * 1000)}`;

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

export default function ControlPage() {
  const [factories] = useState([
    {
      id: "f1",
      name: "Bảo Ân Factory",
      location: "Huế",
      status: "Active",
    },
    {
      id: "f2",
      name: "Anh Khoa Factory",
      location: "Hồ Chí Minh",
      status: "Active",
    },
    {
      id: "f3",
      name: "Đình Duy Factory",
      location: "Gia Lai",
      status: "Inactive",
    },
    {
      id: "f4",
      name: "Trọng Nhã Factory",
      location: "Hà Nội",
      status: "Inactive",
    },
  ]);

  const rows = useMemo(() => {
    return factories.map((f) => ({
      ...f,
      key: f.id || makeId(),
    }));
  }, [factories]);

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left align-top">Factories</h1>
          <p className="mt-1 text-sm text-white/60">Manage your manufacturing facilities</p>
        </div>

        <button
          type="button"
          className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition"
        >
          + Create Factory
        </button>
      </div>

      <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
        <div className="px-5 py-4 border-b border-white/10">
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs uppercase tracking-wider text-white/40">
                <th className="text-left font-medium px-5 py-3">Name</th>
                <th className="text-left font-medium px-5 py-3">Location</th>
                <th className="text-left font-medium px-5 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/10">
              {rows.map((f) => {
                const active = String(f.status).toLowerCase() === "active";
                return (
                  <tr key={f.key} className="hover:bg-white/5 transition">
                    <td className="px-5 py-4 text-left align-top">
                      <div className="text-white font-medium">{f.name}</div>
                      <div className="text-xs text-white/50">ID: {f.id}</div>
                    </td>
                    <td className="px-5 py-4 text-white/70 text-left align-top">{f.location}</td>
                    <td className="px-5 py-4 text-left align-top">
                      <StatusPill active={active} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
