import axios from "axios";
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  withCredentials: true,
  withXSRFToken: true,
  headers: { "Content-Type": "application/json" },
});
api.interceptors.request.use((config) => {
  if (!config.baseURL) throw new Error("Chưa cấu hình VITE_API_BASE_URL.");
  return config;
});
api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const data = error.response?.data as { message?: unknown } | undefined;
      return Promise.reject(
        new Error(
          typeof data?.message === "string"
            ? data.message
            : error.response?.status === 401
              ? "Vui lòng đăng nhập để tiếp tục."
              : "Không thể kết nối máy chủ. Vui lòng thử lại.",
        ),
      );
    }
    return Promise.reject(error);
  },
);
