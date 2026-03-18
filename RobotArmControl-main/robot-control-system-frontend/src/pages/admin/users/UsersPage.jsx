import React, { useEffect, useState } from "react";
import UserTable from "../../../components/users/UserTable";
import CreateUserModal from "../../../components/users/CreateUserModal";
import EditUserModal from "../../../components/users/EditUserModal";
import UserDetailModal from "../../../components/users/UserDetailModal";
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  updateUserRole,
} from "../../../api/userService";
import { getRole, isAdminRole } from "../../../utils/auth";

export default function UsersPage() {
  const canManage = isAdminRole(getRole());

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [detailTarget, setDetailTarget] = useState(null);

  async function loadData() {
    try {
      setLoading(true);
      setError("");
      const data = await getUsers();
      setUsers(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.message || "Failed to load users");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  async function handleCreate(formData) {
    try {
      setSaving(true);
      setError("");
      // Create user without factoryId, status, and role first
      const { factoryId, status, role, ...userCreationData } = formData;
      const created = await createUser(userCreationData);
      
      // Then update with factory, status, and role assignment
      if (factoryId || status) {
        const updateData = { 
          username: created.username,
          email: created.email
        };
        if (factoryId) updateData.factoryId = factoryId;
        if (status) updateData.status = status;

        const updated = await updateUser(created.userId, updateData);
        
        // Use dedicated role endpoint to set role
        if (role) {
          const withRole = await updateUserRole(created.userId, role);
          setUsers((prev) => [withRole, ...prev]);
        } else {
          setUsers((prev) => [updated, ...prev]);
        }
      } else {
        setUsers((prev) => [created, ...prev]);
      }
      
      setCreateOpen(false);
    } catch (e) {
      setError(e?.message || "Failed to create user");
    } finally {
      setSaving(false);
    }
  }

  async function handleEdit(id, formData) {
    try {
      setSaving(true);
      setError("");
      
      // Separate role from other data
      const { role, ...otherData } = formData;
      
      // Update non-role fields first
      const updated = await updateUser(id, otherData);
      
      // Then update role separately if provided
      if (role) {
        const withRole = await updateUserRole(id, role);
        setUsers((prev) => prev.map((u) => (u.userId === id ? withRole : u)));
      } else {
        setUsers((prev) => prev.map((u) => (u.userId === id ? updated : u)));
      }
      
      setEditTarget(null);
    } catch (e) {
      setError(e?.message || "Failed to update user");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(user) {
    if (
      !globalThis.confirm(
        `Delete user "${user.username}"? This action cannot be undone.`
      )
    )
      return;
    try {
      setError("");
      await deleteUser(user.userId);
      setUsers((prev) => prev.filter((u) => u.userId !== user.userId));
    } catch (e) {
      setError(e?.message || "Failed to delete user");
    }
  }

  return (
    <div className="w-full">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-white text-left">Users</h1>
          <p className="mt-1 text-sm text-white/60">Manage user accounts</p>
          {error && <div className="mt-2 text-sm text-red-300">{error}</div>}
        </div>

        {canManage && (
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="h-10 px-4 rounded-lg bg-white text-neutral-950 font-medium hover:bg-white/90 transition disabled:opacity-60"
            disabled={loading}
          >
            + Create User
          </button>
        )}
      </div>

      {canManage && (
        <>
          <CreateUserModal
            open={createOpen}
            onClose={() => setCreateOpen(false)}
            onSubmit={handleCreate}
            loading={saving}
          />

          <EditUserModal
            key={editTarget?.userId ?? "edit-user"}
            open={!!editTarget}
            user={editTarget}
            onClose={() => setEditTarget(null)}
            onSubmit={handleEdit}
            loading={saving}
          />
        </>
      )}

      <UserDetailModal
        open={!!detailTarget}
        user={detailTarget}
        onClose={() => setDetailTarget(null)}
      />

      <div className="mt-6 rounded-2xl border border-white/10 bg-neutral-950/40 overflow-hidden">
        <div className="px-5 py-4 border-b border-white/10">
          <div className="text-xs uppercase tracking-wider text-white/50">
            Users List ({users.length})
          </div>
        </div>
        <UserTable
          users={users}
          loading={loading}
          onView={(u) => setDetailTarget(u)}
          onEdit={canManage ? (u) => setEditTarget(u) : undefined}
          onDelete={canManage ? handleDelete : undefined}
        />
      </div>
    </div>
  );
}
