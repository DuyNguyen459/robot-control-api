// Device API service — TODO: connect to real backend

import axiosClient from "./axiosClient";

export const getDevicesByHub = async (hubId, search = "", status = "") => {
  // TODO: GET /api/devices
const params={};
  if (search) params.search = search;
  if (status) params.status = status;
  const response = await axiosClient.get(`/api/hubs/${hubId}/devices`, {params});
  return response.data.data;
};


export const createDevice = async (hubId, data) => {
  // TODO: POST /api/devices
  const response = await axiosClient.post(`/api/hubs/${hubId}/devices`, data);
  return response.data.data;
};

export const updateDevice = async (deviceId, data) => {
  // TODO: PUT /api/devices/:id
  const response = await axiosClient.put(`/api/devices/${deviceId}`, data);
  return response.data.data;
};

export const deleteDevice = async (deviceId) => {
  // TODO: DELETE /api/devices/:id
  const response = await axiosClient.delete(`/api/devices/${deviceId}`);
  return response.data.data;
}

export const activateDevice = async (deviceId) => {
  //TODO : CHECK STATSU INACTIVE OR ACTIVE, PATCH /api/deivces/{deviceId}/status
  const response = await axiosClient.patch(`/api/devices/${deviceId}/status`);
  return response.data.data;
}

