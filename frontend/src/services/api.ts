import axios from "axios";
import { tokenStore } from "./token";

type Envelope<T> = { success: boolean; message?: string; data: T };

export function normalizeApiBaseUrl(value?: string) {
  const raw = (value || "/api").trim().replace(/\/+$/, "");
  if (!raw || raw === "/") return "/api";
  if (raw.endsWith("/api")) return raw;
  return `${raw}/api`;
}

const baseURL = normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL);

const publicAuthPaths = [
  "/auth/login",
  "/auth/register",
  "/auth/forgot-password",
  "/auth/reset-password",
  "/auth/staff/login",
];

const privateApiPrefixes = [
  "/account",
  "/addresses",
  "/admin",
  "/cart",
  "/coupons",
  "/inventory",
  "/orders",
  "/payments",
  "/review-images",
  "/reviews",
  "/wishlist",
];

const publicPagePaths = [
  "/",
  "/about",
  "/forgot-password",
  "/information",
  "/login",
  "/products",
  "/register",
  "/reset-password",
];

export const AUTH_EXPIRED_EVENT = "net-jewelry-auth-expired";

function pathOnly(url = "") {
  try {
    return new URL(url, "http://local").pathname.replace(/^\/api(?=\/|$)/, "") || "/";
  } catch {
    return url.split("?")[0].replace(/^\/api(?=\/|$)/, "") || "/";
  }
}

function isPublicAuthPath(url = "") {
  const path = pathOnly(url);
  return publicAuthPaths.some((publicPath) => path.startsWith(publicPath));
}

function isPrivateApiPath(url = "") {
  const path = pathOnly(url);
  return path === "/auth/me" || privateApiPrefixes.some((prefix) => path === prefix || path.startsWith(`${prefix}/`));
}

function isPublicPage(pathname: string) {
  return publicPagePaths.some((path) => pathname === path || (path !== "/" && pathname.startsWith(`${path}/`)));
}

function expireStoredSession(redirect = false) {
  tokenStore.clear();
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    if (redirect && !isPublicPage(window.location.pathname)) {
      const from = `${window.location.pathname}${window.location.search}`;
      window.location.replace(`/login?from=${encodeURIComponent(from)}`);
    }
  }
}

function rejectExpiredSession() {
  return Promise.reject(new axios.CanceledError("Phiên đăng nhập đã hết hạn."));
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
      if (isPrivateApiPath(url)) {
        expireStoredSession(true);
        return rejectExpiredSession();
      }
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
      if ((status === 401 || status === 403) && !isPublicAuthPath(url) && isPrivateApiPath(url)) {
        expireStoredSession(true);
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
