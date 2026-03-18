// Area API service
import axiosClient from "./axiosClient";

// GET /api/factories/{factoryId}/areas
export const getAreasByFactory = async (factoryId, search = "", status = "") => {
  const params = {};
  if (search) params.search = search;
  if (status) params.status = status;
  const response = await axiosClient.get(`/api/factories/${factoryId}/areas`, { params });
  return response.data.data;
};

// POST /api/factories/{factoryId}/areas
// Body: { areaName, areaDescription }
export const createArea = async (factoryId, data) => {
  const response = await axiosClient.post(`/api/factories/${factoryId}/areas`, data);
  return response.data.data;
};

// PUT /api/areas/{areaId}
// Body: { areaName, areaDescription }
export const updateArea = async (areaId, data) => {
  const response = await axiosClient.put(`/api/areas/${areaId}`, data);
  return response.data.data;
};

// DELETE /api/areas/{areaId}
export const deleteArea = async (areaId) => {
  const response = await axiosClient.delete(`/api/areas/${areaId}`);
  return response.data.data;
};

// PATCH /api/areas/{areaId}/status (kích hoạt lại area)
export const activateArea = async (areaId) => {
  const response = await axiosClient.patch(`/api/areas/${areaId}/status`);
  return response.data.data;
};