import axiosClient from "./axiosClient";

export const logService = {
  ingest: async (payload) => {
    const response = await axiosClient.post("/api/logs/ingest", payload);
    return response.data?.data;
  },

  getSessionLogs: async (sessionId, { limit = 100 } = {}) => {
    const response = await axiosClient.get(`/api/logs/sessions/${sessionId}`, {
      params: { limit },
    });
    return response.data?.data;
  },
};
