import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import HubTable from "../../../components/hubs/HubTable";
import DeviceTable from "../../../components/devices/DeviceTable";
import CreateDeviceModal from "../../../components/devices/CreateDeviceModal";
import EditDeviceModal from "../../../components/devices/EditDeviceModal";
import { getDevicesByHub, createDevice, updateDevice, deleteDevice } from "../../../api/deviceService";
import { getHubsByArea } from "../../../api/hubService";
import { getAreasByFactory } from "../../../api/areaService";
import { getFactories } from "../../../api/factoryService";
import { getFactoryId, getRole, isAdminRole, isOperatorRole } from "../../../utils/auth";

const SELECTED_HUB_KEY = "adminDevices.selectedHubId";

export default function DevicesPage() {
  const canManage = isAdminRole(getRole());
  const isOperator = isOperatorRole(getRole());
  const navigate = useNavigate();
  const operatorFactoryId = getFactoryId();

  const [selectedHubId, setSelectedHubId] = useState(() => {
    const raw = sessionStorage.getItem(SELECTED_HUB_KEY);
    if (!raw || String(raw).trim() === "") return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  });

  const [devices, setDevices] = useState([]);
  const [hubs, setHubs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  const [sessionSelectedDeviceId, setSessionSelectedDeviceId] = useState(() => {
    const raw = sessionStorage.getItem("robotSession.deviceId");
    if (raw == null || String(raw).trim() === "") return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  });

  async function loadHubs() {
    try {
      setLoading(true);
      setError("");

      if (isOperator) {
        if (!operatorFactoryId) {
          setHubs([]);
          setError("No factory is assigned to this operator.");
          return;
        }

        const areasData = await getAreasByFactory(operatorFactoryId);
        const areas = Array.isArray(areasData) ? areasData : [];

        const allHubs = [];
        for (const a of areas) {
          try {
            const hubsData = await getHubsByArea(a.areaId);
            if (Array.isArray(hubsData)) {
              hubsData.forEach((h) => {
                h.areaName = a.areaName;
              });
              allHubs.push(...hubsData);
            }
          } catch {
            /* skip */
          }
        }

        setHubs(allHubs);
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

      const allHubs = [];
      for (const a of allAreas) {
        try {
          const hubsData = await getHubsByArea(a.areaId);
          if (Array.isArray(hubsData)) {
            hubsData.forEach((h) => {
              h.areaName = a.areaName;
            });
            allHubs.push(...hubsData);
          }
        } catch {
          /* skip */
        }
      }

      setHubs(allHubs);
    } catch (e) {
      setError(e?.message || "Failed to load hubs");
    } finally {
      setLoading(false);
    }
  }

  async function loadDevicesForHub(hubId) {
    if (!hubId) {
      setDevices([]);
      return;
    }
    try {
      setLoading(true);
      setError("");
      const selectedHub = hubs.find((h) => h.hubId === hubId);
      const devicesData = await getDevicesByHub(hubId);
      const list = Array.isArray(devicesData) ? devicesData : [];
      list.forEach((d) => {
        d.hubName = selectedHub?.hubName || `Hub #${hubId}`;
      });
      setDevices(list);
    } catch (e) {
      setError(e?.message || "Failed to load devices");
      setDevices([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadHubs(); }, []);

  useEffect(() => {
    if (!hubs || hubs.length === 0) {
      setDevices([]);
      return;
    }
    if (selectedHubId) {
      loadDevicesForHub(selectedHubId);
    } else {
      setDevices([]);
    }
  }, [selectedHubId, hubs]);

  const canCreate = canManage && !!selectedHubId;

  async function handleCreate(formData) {
    try {
      setSaving(true);
      setError("");
      const { hubId, ...rest } = formData;
      const hub = hubs.find((h) => h.hubId === hubId);
      const created = await createDevice(hubId, rest);
      created.hubName = hub?.hubName || `Hub #${hubId}`;
      setDevices((prev) => (hubId === selectedHubId ? [created, ...prev] : prev));
      setCreateOpen(false);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to create device");
    } finally {
      setSaving(false);
    }
  }

  async function handleEdit(deviceId, formData) {
    try {
      setSaving(true);
      setError("");
      const updated = await updateDevice(deviceId, formData);
      setDevices((prev) => prev.map((d) => {
        if (d.deviceId === deviceId) return { ...updated, hubName: d.hubName };
        return d;
      }));
      setEditTarget(null);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to update device");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(device) {
    if (!window.confirm(`Delete "${device.deviceName}"? This action cannot be undone.`)) return;
    try {
      setError("");
      await deleteDevice(device.deviceId);
      setDevices((prev) => prev.filter((d) => d.deviceId !== device.deviceId));
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || "Failed to delete device");
    }
  }

  const selectedHub = selectedHubId ? hubs.find((h) => h.hubId === selectedHubId) : null;

  function handleSelectDevice(device) {
    if (!device?.deviceId) return;
    sessionStorage.setItem("robotSession.deviceId", String(device.deviceId));
    setSessionSelectedDeviceId(device.deviceId);
    
    if (isOperator) {
      navigate("/admin/ai-camera");
    }
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left">Devices</h1>
          <p className="mt-1 text-sm text-white/60">Manage devices within your hubs</p>
          <div className="mt-2 text-xs text-white/50">Selected Device: {sessionSelectedDeviceId ?? "—"}</div>
          {error && <div className="mt-2 text-sm text-red-300">{error}</div>}
        </div>

        {canCreate ? (
          <button type="button" onClick={() => setCreateOpen(true)} className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60" disabled={loading}>
            + Create Device
          </button>
        ) : canManage ? (
          <div className="h-10 px-4 rounded-lg bg-yellow-500/10 border border-yellow-500/20 flex items-center text-sm text-yellow-300">
            Select a hub before creating devices.
          </div>
        ) : null}
      </div>

      {canManage && (
        <>
          <CreateDeviceModal open={createOpen} onClose={() => setCreateOpen(false)} onSubmit={handleCreate} loading={saving} hubs={hubs} initialHubId={selectedHubId} />
          <EditDeviceModal open={!!editTarget} device={editTarget} onClose={() => setEditTarget(null)} onSubmit={handleEdit} loading={saving} />
        </>
      )}

      {!selectedHubId ? (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10">
            <div className="text-xs uppercase tracking-wider text-white/50">Select Hub ({hubs.length})</div>
          </div>
          <HubTable
            hubs={hubs}
            loading={loading}
            onRowClick={(h) => {
              const id = Number(h?.hubId);
              if (!Number.isFinite(id)) return;
              sessionStorage.setItem(SELECTED_HUB_KEY, String(id));
              setSelectedHubId(id);
            }}
          />
        </div>
      ) : (
        <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
          <div className="px-5 py-4 border-b border-white/10 flex items-center justify-between gap-3">
            <div className="text-xs uppercase tracking-wider text-white/50">Devices for {selectedHub?.hubName || `Hub #${selectedHubId}`} ({devices.length})</div>
            <button
              type="button"
              className="h-9 px-3 rounded-lg bg-white/10 text-white hover:bg-white/15 transition"
              onClick={() => {
                sessionStorage.removeItem(SELECTED_HUB_KEY);
                setSelectedHubId(null);
              }}
              disabled={loading}
            >
              Change Hub
            </button>
          </div>
          <DeviceTable
            devices={devices}
            loading={loading}
            onEdit={canManage ? (d) => setEditTarget(d) : undefined}
            onDelete={canManage ? handleDelete : undefined}
            onRowClick={handleSelectDevice}
          />
        </div>
      )}
    </div>
  );
}