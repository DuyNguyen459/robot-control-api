// Hub API service — TODO: connect to real backend
import axiosClient from "./axiosClient";



export const getHubsByArea = async (areaId, search = "", status = "") => {
  // TODO: GET /api/areas/:areaId/hubs
const params = {};
  if (search) params.search = search;
  if (status) params.status = status;
  const response = await axiosClient.get(`/api/areas/${areaId}/hubs`, { params });
  return response.data.data;
};

export const createHub = async (areaId,data) => {
  // TODO: POST /api/hubs
  const response = await axiosClient.post(`/api/areas/${areaId}/hubs`, data);
  return response.data.data;
};

export const updateHub = async (hubId, data) => {
  // TODO: PUT /api/hubs/:id
  const response = await axiosClient.put(`/api/hubs/${hubId}`, data);
  return response.data.data;
};

export const deleteHub = async (hubId) => {
  // TODO: DELETE /api/hubs/:id
  const response = await axiosClient.delete(`/api/hubs/${hubId}`);
  return response.data.data;

};

export const activateHub = async (hubId) => {
  //TODO: PATCH /api/hubs/:hubId/status
  const response = await axiosClient.patch(`/api/hubs/${hubId}/status`);
  return response.data.data;
};
