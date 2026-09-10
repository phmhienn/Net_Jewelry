import { api } from "./api";
import { isDemo } from "../data/config";
import type { User } from "../types";
const demoUser: User = {
  id: "demo",
  name: "Minh Anh",
  email: "demo@netjewelry.vn",
};
function saveSession(user: User | null) {
  try {
    if (user) sessionStorage.setItem("net-demo-session", JSON.stringify(user));
    else sessionStorage.removeItem("net-demo-session");
  } catch {
    /* Demo session remains available in memory. */
  }
  return user;
}
export const authService = {
  async me(): Promise<User | null> {
    if (isDemo) {
      try {
        return JSON.parse(
          sessionStorage.getItem("net-demo-session") ?? "null",
        ) as User | null;
      } catch {
        return null;
      }
    }
    const response = await api.get<User>("/auth/me", {
      validateStatus: (status) =>
        status === 401 || (status >= 200 && status < 300),
    });
    return response.status === 401 ? null : response.data;
  },
  async login(email: string, password: string): Promise<User> {
    if (!isDemo)
      return (await api.post<User>("/auth/login", { email, password })).data;
    if (
      email.trim().toLowerCase() !== demoUser.email ||
      password !== "NetDemo123!"
    )
      throw new Error("Tài khoản demo: demo@netjewelry.vn / NetDemo123!");
    saveSession(demoUser);
    return demoUser;
  },
  async register(name: string, email: string, password: string): Promise<User> {
    if (!isDemo)
      return (await api.post<User>("/auth/register", { name, email, password }))
        .data;
    const user = { id: crypto.randomUUID(), name, email };
    saveSession(user);
    return user;
  },
  async logout() {
    if (!isDemo) await api.post("/auth/logout");
    else saveSession(null);
  },
  async update(user: User): Promise<User> {
    if (!isDemo)
      return (
        await api.patch<User>("/account", {
          name: user.name,
          phone: user.phone,
        })
      ).data;
    saveSession(user);
    return user;
  },
};
