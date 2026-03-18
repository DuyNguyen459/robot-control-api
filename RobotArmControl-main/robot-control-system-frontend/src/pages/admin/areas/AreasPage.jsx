import React, { useEffect, useState } from "react";
import FactoryTable from "../../../components/factories/FactoryTable";
import AreaTable from "../../../components/areas/AreaTable";
import CreateAreaModal from "../../../components/areas/CreateAreaModal";
import EditAreaModal from "../../../components/areas/EditAreaModal";
import { getAreasByFactory, createArea, updateArea, deleteArea } from "../../../api/areaService";
import { getFactories, getFactoryById } from "../../../api/factoryService";
import { getFactoryId, getRole, isAdminRole, isOperatorRole } from "../../../utils/auth";

const SELECTED_FACTORY_KEY = "adminAreas.selectedFactoryId";

export default function AreasPage() {
  const canManage = isAdminRole(getRole());
  const isOperator = isOperatorRole(getRole());
  const operatorFactoryId = getFactoryId();

  const [selectedFactoryIdState, setSelectedFactoryIdState] = useState(() => {
    const raw = sessionStorage.getItem(SELECTED_FACTORY_KEY);
    if (!raw || String(raw).trim() === "") return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  });

  const selectedFactoryId = isOperator ? (operatorFactoryId || null) : selectedFactoryIdState;

  const [areas, setAreas] = useState([]);
  const [factories, setFactories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  async function loadFactories() {
    try {
      setLoading(true);
      setError("");
      if (isOperator) {
        if (!operatorFactoryId) {
          setFactories([]);
          setError("No factory is assigned to this operator.");
          return;
        }
        // Best-effort: prefer list+filter (often allowed), fall back to by-id, and
        // finally to a minimal placeholder so Areas can still load.
        try {
          const factoriesData = await getFactories();
          const list = Array.isArray(factoriesData) ? factoriesData : [];
          const found = list.find((f) => Number(f?.factoryId) === Number(operatorFactoryId));
          if (found) {
            setFactories([found]);
            return;
          }
        } catch {
          // ignore
        }

        try {
          const factory = await getFactoryById(operatorFactoryId);
          setFactories(factory ? [factory] : [{ factoryId: operatorFactoryId, factoryName: `Factory #${operatorFactoryId}` }]);
        } catch {
          setFactories([{ factoryId: operatorFactoryId, factoryName: `Factory #${operatorFactoryId}` }]);
        }
        return;
      }

      const factoriesData = await getFactories();
      const factoryList = Array.isArray(factoriesData) ? factoriesData : [];
      setFactories(factoryList);
    } catch (e) {
      setError(e?.message || "Failed to load data");
    } finally {
      setLoading(false);
    }
  }

  async function loadAreasForFactory(factoryId) {
    if (!factoryId) {
      setAreas([]);
      return;
    }
    try {
      setLoading(true);
      setError("");
      const selectedFactory = factories.find((f) => f.factoryId === factoryId);
      const areasData = await getAreasByFactory(factoryId);
      const list = Array.isArray(areasData) ? areasData : [];
      list.forEach((a) => {
        a.factoryName = selectedFactory?.factoryName || `Factory #${factoryId}`;
      });
      setAreas(list);
    } catch (e) {
      setError(e?.message || "Failed to load areas");
      setAreas([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadFactories(); }, []);

  useEffect(() => {
    if (selectedFactoryId) {
      loadAreasForFactory(selectedFactoryId);
    } else {
      setAreas([]);
    }
  }, [selectedFactoryId, factories]);

  const canCreate = canManage && !!selectedFactoryId;

  async function handleCreate(formData) {
    try {
      setSaving(true);
      setError("");
      const { factoryId, areaName, areaDescription } = formData;
      const created = await createArea(factoryId, { areaName, areaDescription });
      const factory = factories.find((f) => f.factoryId === factoryId);
      created.factoryName = factory?.factoryName || `Factory #${factoryId}`;
      setAreas((prev) => (factoryId === selectedFactoryId ? [created, ...prev] : prev));
      setCreateOpen(false);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to create area");
    } finally {
      setSaving(false);
    }
  }

  async function handleEdit(areaId, formData) {
    try {
      setSaving(true);
      setError("");
      const updated = await updateArea(areaId, formData);
      setAreas((prev) => prev.map((a) => {
        if (a.areaId === areaId) {
          return { ...updated, factoryName: a.factoryName };
        }
        return a;
      }));
      setEditTarget(null);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to update area");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(area) {
    if (!window.confirm(`Delete "${area.areaName}"? This action cannot be undone.`)) return;
    try {
      setError("");
      await deleteArea(area.areaId);
      setAreas((prev) => prev.filter((a) => a.areaId !== area.areaId));
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to delete area");
    }
  }

  const selectedFactory = selectedFactoryId ? factories.find((f) => f.factoryId === selectedFactoryId) : null;

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left">Areas</h1>
          <p className="mt-1 text-sm text-white/60">Manage areas within your factories</p>
          {error && <div className="mt-2 text-sm text-red-300">{error}</div>}
        </div>

        {canCreate ? (
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            disabled={loading}
          >
            + Create Area
          </button>
        ) : canManage ? (
          <div className="h-10 px-4 rounded-lg bg-yellow-500/10 border border-yellow-500/20 flex items-center text-sm text-yellow-300">
            Select a factory before creating areas.
          </div>
        ) : null}
      </div>

      {canManage && (
        <>
          <CreateAreaModal
            open={createOpen}
            onClose={() => setCreateOpen(false)}
            onSubmit={handleCreate}
            loading={saving}
            factories={factories}
            initialFactoryId={selectedFactoryId}
          />

          <EditAreaModal
            key={editTarget?.areaId ?? "edit-area"}
            open={!!editTarget}
            area={editTarget}
            onClose={() => setEditTarget(null)}
            onSubmit={handleEdit}
            loading={saving}
          />
        </>
      )}

      {!selectedFactoryId ? (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10">
            <div className="text-xs uppercase tracking-wider text-white/50">
              Select Factory ({factories.length})
            </div>
          </div>
          <FactoryTable
            factories={factories}
            loading={loading}
            onRowClick={(f) => {
              const id = Number(f?.factoryId);
              if (!Number.isFinite(id)) return;
              sessionStorage.setItem(SELECTED_FACTORY_KEY, String(id));
              setSelectedFactoryIdState(id);
            }}
          />
        </div>
      ) : (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10 flex items-center justify-between gap-3">
            <div className="text-xs uppercase tracking-wider text-white/50">
              Areas for {selectedFactory?.factoryName || `Factory #${selectedFactoryId}`} ({areas.length})
            </div>
            {!isOperator && (
              <button
                type="button"
                className="h-9 px-3 rounded-lg bg-white/10 text-white hover:bg-white/15 transition"
                onClick={() => {
                  sessionStorage.removeItem(SELECTED_FACTORY_KEY);
                  setSelectedFactoryIdState(null);
                }}
                disabled={loading}
              >
                Change Factory
              </button>
            )}
          </div>
          <AreaTable
            areas={areas}
            loading={loading}
            onEdit={canManage ? (a) => setEditTarget(a) : undefined}
            onDelete={canManage ? handleDelete : undefined}
          />
        </div>
      )}
    </div>
  );
}