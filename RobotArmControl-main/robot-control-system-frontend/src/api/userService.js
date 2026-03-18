import axiosClient from "./axiosClient";

// Get all users
const getUsersPage = async ({ page, size, role, status, search, sortBy, sortDir }) => {
  const params = { page, size, sortBy, sortDir };
  if (role) params.role = role;
  if (status) params.status = status;
  if (search) params.search = search;

  const res = await axiosClient.get("/api/users", { params });
  return res.data?.data;
};

export const getUsers = async (options = {}) => {
  const {
    size = 100,
    role,
    status,
    search,
    sortBy = "userId",
    sortDir = "asc",
  } = options;

  const first = await getUsersPage({ page: 0, size, role, status, search, sortBy, sortDir });

  // Backward compatibility: if API ever returns a plain array
  if (Array.isArray(first)) return first;

  const firstContent = Array.isArray(first?.content) ? first.content : [];
  const totalPages = Number(first?.totalPages || 1);

  if (!Number.isFinite(totalPages) || totalPages <= 1) return firstContent;

  const pages = await Promise.all(
    Array.from({ length: totalPages - 1 }, (_, i) =>
      getUsersPage({ page: i + 1, size, role, status, search, sortBy, sortDir })
    )
  );

  const rest = pages.flatMap((p) => (Array.isArray(p?.content) ? p.content : []));
  return [...firstContent, ...rest];
};

// Get user by ID
export const getUserById = async (userId) => {
  const res = await axiosClient.get(`/api/users/${userId}`);
  return res.data.data;
};

// Create new user (register)
export const createUser = async (data) => {
  const res = await axiosClient.post("/api/auth/register", data);
  return res.data.data;
};

// Update user
export const updateUser = async (userId, data) => {
  const res = await axiosClient.put(`/api/users/${userId}`, data);
  return res.data.data;
};

// Delete user
export const deleteUser = async (userId) => {
  const res = await axiosClient.delete(`/api/users/${userId}`);
  return res.data.data;
};

// Update user status
export const updateUserStatus = async (userId, status) => {
  const res = await axiosClient.patch(`/api/users/${userId}/status`, { status });
  return res.data.data;
};

// Update user role
export const updateUserRole = async (userId, role) => {
  const res = await axiosClient.patch(`/api/users/${userId}/role`, { role });
  return res.data.data;
};
