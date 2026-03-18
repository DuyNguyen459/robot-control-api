import React, { useEffect, useState } from "react";
import AreaTable from "../../../components/areas/AreaTable";
import HubTable from "../../../components/hubs/HubTable";
import CreateHubModal from "../../../components/hubs/CreateHubModal";
import EditHubModal from "../../../components/hubs/EditHubModal";
import { getHubsByArea, createHub, updateHub, deleteHub } from "../../../api/hubService";
import { getAreasByFactory } from "../../../api/areaService";
import { getFactories } from "../../../api/factoryService";
import { getFactoryId, getRole, isAdminRole, isOperatorRole } from "../../../utils/auth";

const SELECTED_AREA_KEY = "adminHubs.selectedAreaId";

export default function HubsPage() {
  const canManage = isAdminRole(getRole());
  const isOperator = isOperatorRole(getRole());
  const operatorFactoryId = getFactoryId();

  const [selectedAreaId, setSelectedAreaId] = useState(() => {
    const raw = sessionStorage.getItem(SELECTED_AREA_KEY);
    if (!raw || String(raw).trim() === "") return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  });

  const [hubs, setHubs] = useState([]);
  const [areas, setAreas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  async function loadAreas() {
    try {
      setLoading(true);
      setError("");
      if (isOperator) {
        if (!operatorFactoryId) {
          setAreas([]);
          setError("No factory is assigned to this operator.");
          return;
        }

        const areasData = await getAreasByFactory(operatorFactoryId);
        const list = Array.isArray(areasData) ? areasData : [];
        setAreas(list);
        return;
      }

      const factoriesData = await getFactories();
      const factoryList = Array.isArray(factoriesData) ? factoriesData : [];

      const allAreas = [];
      for (const f of factoryList) {
        try {
          const areasData = await getAreasByFactory(f.factoryId);
          if (Array.isArray(areasData)) {
            areasData.forEach((a) => {
              a.factoryName = f.factoryName;
            });
            allAreas.push(...areasData);
          }
        } catch {
          /* skip */
        }
      }

      setAreas(allAreas);
    } catch (e) {
      setError(e?.message || "Failed to load areas");
    } finally {
      setLoading(false);
    }
  }

  async function loadHubsForArea(areaId) {
    if (!areaId) {
      setHubs([]);
      return;
    }
    try {
      setLoading(true);
      setError("");
      const selectedArea = areas.find((a) => a.areaId === areaId);
      const hubsData = await getHubsByArea(areaId);
      const list = Array.isArray(hubsData) ? hubsData : [];
      list.forEach((h) => {
        h.areaName = selectedArea?.areaName || `Area #${areaId}`;
      });
      setHubs(list);
    } catch (e) {
      setError(e?.message || "Failed to load hubs");
      setHubs([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadAreas(); }, []);

  useEffect(() => {
    if (!areas || areas.length === 0) {
      setHubs([]);
      return;
    }
    if (selectedAreaId) {
      loadHubsForArea(selectedAreaId);
    } else {
      setHubs([]);
    }
  }, [selectedAreaId, areas]);

  const canCreate = canManage && !!selectedAreaId;

async function handleCreate(formData) {
  try {
    setSaving(true);
    setError("");
    const { areaId, ...rest } = formData;
    const area = areas.find((a) => a.areaId === areaId);
    const created = await createHub(areaId, rest);
    created.areaName = area?.areaName || `Area #${areaId}`;
    setHubs((prev) => (areaId === selectedAreaId ? [created, ...prev] : prev));
    setCreateOpen(false);
  } catch (e) {
    setError(e?.response?.data?.message || e?.message || "Failed to create hub");
  } finally {
    setSaving(false);
  }
}

  async function handleEdit(id, formData) {
    try {
      setSaving(true);
      setError("");
      const updated = await updateHub(id, formData);
      setHubs((prev) => prev.map((h) => (h.hubId === id ? { ...updated, areaName: h.areaName } : h)));
      setEditTarget(null);
    } catch (e) {
      setError(e?.message || "Failed to update hub");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(hub) {
    if (!window.confirm(`Delete "${hub.hubName}"? This action cannot be undone.`)) return;
    try {
      setError("");
      await deleteHub(hub.hubId);
      setHubs((prev) => prev.filter((h) => h.hubId !== hub.hubId));
    } catch (e) {
      setError(e?.message || "Failed to delete hub");
    }
  }

  const selectedArea = selectedAreaId ? areas.find((a) => a.areaId === selectedAreaId) : null;

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left">Hubs</h1>
          <p className="mt-1 text-sm text-white/60">Manage hubs within your areas</p>
          {error && <div className="mt-2 text-sm text-red-300">{error}</div>}
        </div>

        {canCreate ? (
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            disabled={loading}
          >
            + Create Hub
          </button>
        ) : canManage ? (
          <div className="h-10 px-4 rounded-lg bg-yellow-500/10 border border-yellow-500/20 flex items-center text-sm text-yellow-300">
            Select an area before creating hubs.
          </div>
        ) : null}
      </div>

      {canManage && (
        <>
          <CreateHubModal
            open={createOpen}
            onClose={() => setCreateOpen(false)}
            onSubmit={handleCreate}
            loading={saving}
            areas={areas}
            initialAreaId={selectedAreaId}
          />

          <EditHubModal
            key={editTarget?.hubId ?? "edit-hub"}
            open={!!editTarget}
            hub={editTarget}
            onClose={() => setEditTarget(null)}
            onSubmit={handleEdit}
            loading={saving}
            areas={areas}
          />
        </>
      )}

      {!selectedAreaId ? (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10">
            <div className="text-xs uppercase tracking-wider text-white/50">Select Area ({areas.length})</div>
          </div>
          <AreaTable
            areas={areas}
            loading={loading}
            onRowClick={(a) => {
              const id = Number(a?.areaId);
              if (!Number.isFinite(id)) return;
              sessionStorage.setItem(SELECTED_AREA_KEY, String(id));
              setSelectedAreaId(id);
            }}
          />
        </div>
      ) : (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10 flex items-center justify-between gap-3">
            <div className="text-xs uppercase tracking-wider text-white/50">
              Hubs for {selectedArea?.areaName || `Area #${selectedAreaId}`} ({hubs.length})
            </div>
            <button
              type="button"
              className="h-9 px-3 rounded-lg bg-white/10 text-white hover:bg-white/15 transition"
              onClick={() => {
                sessionStorage.removeItem(SELECTED_AREA_KEY);
                setSelectedAreaId(null);
              }}
              disabled={loading}
            >
              Change Area
            </button>
          </div>
          <HubTable
            hubs={hubs}
            loading={loading}
            onEdit={canManage ? (h) => setEditTarget(h) : undefined}
            onDelete={canManage ? handleDelete : undefined}
          />
        </div>
      )}
    </div>
  );
}
