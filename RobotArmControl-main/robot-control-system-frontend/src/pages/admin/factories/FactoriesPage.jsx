import React, { useEffect, useState } from "react";
import FactoryTable from "../../../components/factories/FactoryTable";
import CreateFactoryModal from "../../../components/factories/CreateFactoryModal";
import EditFactoryModal from "../../../components/factories/EditFactoryModal";
import {
  getFactories,
  getFactoryById,
  createFactory,
  updateFactory,
  deleteFactory,
} from "../../../api/factoryService";
import { getFactoryId, getRole, isAdminRole, isOperatorRole } from "../../../utils/auth";

export default function FactoriesPage() {
  const canManage = isAdminRole(getRole());
  const isOperator = isOperatorRole(getRole());
  const operatorFactoryId = getFactoryId();

  const [factories, setFactories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  async function loadData() {
    try {
      setLoading(true);
      setError("");
      if (isOperator) {
        if (!operatorFactoryId) {
          setFactories([]);
          setError("No factory is assigned to this operator.");
          return;
        }
        // Some backends may not allow OPERATOR to call GET /api/factories/{id}.
        // Prefer listing and filtering; fall back to by-id if needed.
        let filtered = [];
        try {
          const data = await getFactories();
          const list = Array.isArray(data) ? data : [];
          const found = list.find((f) => Number(f?.factoryId) === Number(operatorFactoryId));
          filtered = found ? [found] : [];
        } catch {
          filtered = [];
        }

        if (filtered.length > 0) {
          setFactories(filtered);
          return;
        }

        const factory = await getFactoryById(operatorFactoryId);
        setFactories(factory ? [factory] : []);
        return;
      }

      const data = await getFactories();
      const list = Array.isArray(data) ? data : [];
      setFactories(list);
    } catch (e) {
      setError(e?.message || "Failed to load factories");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadData(); }, []);

  async function handleCreate(formData) {
    try {
      setSaving(true);
      setError("");
      const created = await createFactory(formData);
      setFactories((prev) => [created, ...prev]);
      setCreateOpen(false);
    } catch (e) {
      setError(e?.message || "Failed to create factory");
    } finally {
      setSaving(false);
    }
  }

  async function handleEdit(id, formData) {
    try {
      setSaving(true);
      setError("");
      const updated = await updateFactory(id, formData);
      setFactories((prev) => prev.map((f) => (f.factoryId === id ? updated : f)));
      setEditTarget(null);
    } catch (e) {
      setError(e?.message || "Failed to update factory");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(factory) {
    if (!window.confirm(`Delete "${factory.factoryName}"? This action cannot be undone.`)) return;
    try {
      setError("");
      await deleteFactory(factory.factoryId);
      setFactories((prev) => prev.filter((f) => f.factoryId !== factory.factoryId));
    } catch (e) {
      setError(e?.message || "Failed to delete factory");
    }
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left">Factories</h1>
          <p className="mt-1 text-sm text-white/60">Manage your manufacturing facilities</p>
          {error && <div className="mt-2 text-sm text-red-300">{error}</div>}
        </div>

        {canManage && (
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            disabled={loading}
          >
            + Create Factory
          </button>
        )}
      </div>

      {canManage && (
        <>
          <CreateFactoryModal
            open={createOpen}
            onClose={() => setCreateOpen(false)}
            onSubmit={handleCreate}
            loading={saving}
          />

          <EditFactoryModal
            key={editTarget?.factoryId ?? "edit-factory"}
            open={!!editTarget}
            factory={editTarget}
            onClose={() => setEditTarget(null)}
            onSubmit={handleEdit}
            loading={saving}
          />
        </>
      )}

      <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
        <div className="px-5 py-4 border-b border-white/10">
          <div className="text-xs uppercase tracking-wider text-white/50">
            Factories List ({factories.length})
          </div>
        </div>
        <FactoryTable
          factories={factories}
          loading={loading}
          onEdit={canManage ? (f) => setEditTarget(f) : undefined}
          onDelete={canManage ? handleDelete : undefined}
        />
      </div>
    </div>
  );
}
