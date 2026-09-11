const KEY = "net-jewelry-token";

function readPayload(token: string): { exp?: number } | null {
  const [, payload] = token.split(".");
  if (!payload) return null;

  try {
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    return JSON.parse(atob(padded)) as { exp?: number };
  } catch {
    return null;
  }
}

export const tokenStore = {
  get() {
    try {
      return localStorage.getItem(KEY);
    } catch {
      return null;
    }
  },
  set(token: string) {
    try {
      localStorage.setItem(KEY, token);
    } catch {
      /* Token remains unavailable if storage is blocked. */
    }
  },
  clear() {
    try {
      localStorage.removeItem(KEY);
    } catch {
      /* Ignore storage cleanup failures. */
    }
  },
  isExpired(skewSeconds = 10) {
    const token = this.get();
    if (!token) return true;

    const payload = readPayload(token);
    if (!payload?.exp) return true;

    return Date.now() / 1000 >= payload.exp - skewSeconds;
  },
};
