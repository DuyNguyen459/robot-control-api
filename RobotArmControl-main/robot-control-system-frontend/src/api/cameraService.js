import axiosClient from "./axiosClient";

export const cameraService = {
  start: async (deviceId) => {
    const body = { controlMode: "CAMERA" };
    if (deviceId != null) body.deviceId = deviceId;

    // Use the device-aware endpoint.
    const response = await axiosClient.post("/api/control-sessions", body);
    return response.data?.data;
  },

  stop: async () => {
    // Stop current session.
    const response = await axiosClient.patch("/api/control-sessions/current/status");
    return response.data?.data;
  },

  status: async () => {
    const response = await axiosClient.get("/api/control-sessions/current");
    return response.data?.data;
  },

  sendAngles: async (angles, deviceId) => {
    const response = await axiosClient.post("/api/camera/angles", { angles, deviceId });
    return response.data?.data;
  },
};
