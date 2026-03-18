import axios from "axios";

const axiosClient = axios.create({
  baseURL: "",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

axiosClient.interceptors.request.use((config) => {

  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;

});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {

  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });

  failedQueue = [];

};

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {

    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      originalRequest.url?.includes("/auth/login")
    ) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401) {

      if (!originalRequest._retry && localStorage.getItem("refreshToken")) {

        if (isRefreshing) {

          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then((token) => {
            originalRequest.headers.Authorization = "Bearer " + token;
            return axiosClient(originalRequest);
          });

        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {

          const refreshToken = localStorage.getItem("refreshToken");

          const res = await axios.post(
            "/auth/refresh",
            { refreshToken }
          );

          const newToken = res.data.token;

          localStorage.setItem("token", newToken);

          processQueue(null, newToken);

          originalRequest.headers.Authorization = "Bearer " + newToken;

          return axiosClient(originalRequest);

        } catch (err) {

          processQueue(err, null);

          localStorage.removeItem("token");
          localStorage.removeItem("refreshToken");

          window.location.replace("/");

          return Promise.reject(err);

        } finally {

          isRefreshing = false;

        }

      }

    }

    return Promise.reject(error);

  }
);

export default axiosClient;