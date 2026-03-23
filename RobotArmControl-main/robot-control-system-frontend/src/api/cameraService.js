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

  sendRobotControl: async ({ robotId, jointAngles, gripper = null, timestamp = null }) => {
    const payload = {
      robotId,
      jointAngles,
      timestamp: timestamp || new Date().toISOString(),
    };

    if (gripper !== null && gripper !== undefined) {
      payload.gripper = gripper;
    }

    try {
      const response = await axiosClient.post("/api/robot-control/commands", payload);
      return response.data?.data;
    } catch (error) {
      const status = error?.response?.status;
      const shouldFallback = status === 404 || status === 405;
      if (!shouldFallback) {
        throw error;
      }

      const legacyAnglesResp = await axiosClient.post("/api/camera/angles", {
        angles: jointAngles,
        deviceId: robotId,
      });

      if (gripper !== null && gripper !== undefined) {
        const action = Number(gripper) === 1 ? "grab" : "release";
        await axiosClient.post("/api/camera/commands", {
          action,
          deviceId: robotId,
        });
      }

      return legacyAnglesResp.data?.data;
    }
  },

  // Backward-compatible helper used by current hook code.
  sendAngles: async (angles, robotId) => {
    return cameraService.sendRobotControl({
      robotId,
      jointAngles: angles,
    });
  },

  sendGripperAction: async (action, robotId, jointAngles = [0, 0, 0, 0, 0, 0]) => {
    const normalized = String(action || "").trim().toLowerCase();
    const gripper = normalized === "grab" ? 1 : 0;
    return cameraService.sendRobotControl({
      robotId,
      jointAngles,
      gripper,
    });
  },
};
