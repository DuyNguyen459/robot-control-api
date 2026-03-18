import axiosClient from "./axiosClient";

export const authApi = {
  login: async (credentials) => {
    const response = await axiosClient.post("/api/auth/login", credentials);

    const payload = response?.data;
    if (payload?.success === false) {
      const err = new Error(payload?.message || "Login failed");
      err.code = payload?.code;
      throw err;
    }

    const authData = response.data.data;

    if (!authData?.accessToken) {
      const err = new Error(payload?.message || "Login failed");
      err.code = payload?.code;
      throw err;
    }

    // lưu token
    localStorage.setItem("token", authData.accessToken);
    localStorage.setItem("role", authData.role);
    localStorage.setItem("username", authData.username);
    if (authData?.email) {
      localStorage.setItem("email", authData.email);
    } else {
      localStorage.removeItem("email");
    }
    if (authData?.factoryId != null) {
      localStorage.setItem("factoryId", String(authData.factoryId));
    } else {
      localStorage.removeItem("factoryId");
    }

    return authData;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    localStorage.removeItem("email");
    localStorage.removeItem("factoryId");
  },
};