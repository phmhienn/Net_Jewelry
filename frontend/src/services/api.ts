import axios from "axios";
import { tokenStore } from "./token";

type Envelope<T> = { success: boolean; message?: string; data: T };

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api";

const publicAuthPaths = [
  "/auth/login",
  "/auth/register",
  "/auth/forgot-password",
  "/auth/reset-password",
  "/auth/staff/login",
];

export const AUTH_EXPIRED_EVENT = "net-jewelry-auth-expired";

function isPublicAuthPath(url = "") {
  return publicAuthPaths.some((path) => url.startsWith(path));
}

function expireStoredSession() {
  tokenStore.clear();
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
  }
}

export const api = axios.create({
  baseURL,
  timeout: 20000,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const url = config.url ?? "";
  if (!isPublicAuthPath(url)) {
    if (tokenStore.isExpired()) {
      tokenStore.clear();
    } else {
      const token = tokenStore.get();
      if (token) config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    const body = response.data as Envelope<unknown> | unknown;
    if (
      body &&
      typeof body === "object" &&
      "success" in body &&
      "data" in body
    ) {
      response.data = (body as Envelope<unknown>).data;
    }
    return response;
  },
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const status = error.response?.status;
      const url = error.config?.url ?? "";
      if ((status === 401 || status === 403) && !isPublicAuthPath(url)) {
        expireStoredSession();
      }

      const data = error.response?.data as
        | { message?: unknown; fieldErrors?: Record<string, string> }
        | undefined;
      const fieldMessage = data?.fieldErrors
        ? Object.values(data.fieldErrors).find(Boolean)
        : undefined;
      return Promise.reject(
        new Error(
          typeof fieldMessage === "string"
            ? fieldMessage
            : typeof data?.message === "string"
              ? data.message
              : status === 401
                ? "Vui lòng đăng nhập để tiếp tục."
                : status === 403
                  ? "Bạn không có quyền thực hiện thao tác này."
                  : "Không thể kết nối máy chủ. Vui lòng thử lại.",
        ),
      );
    }
    return Promise.reject(error);
  },
);
