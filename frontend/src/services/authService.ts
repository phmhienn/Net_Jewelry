import { api } from "./api";
import { tokenStore } from "./token";
import type { User } from "../types";
import { type BackendLogin, type BackendUser, mapUser } from "./backendTypes";

export const authService = {
  async me(): Promise<User | null> {
    if (!tokenStore.get()) return null;
    if (tokenStore.isExpired()) {
      tokenStore.clear();
      return null;
    }

    try {
      return mapUser((await api.get<BackendUser>("/auth/me")).data);
    } catch {
      tokenStore.clear();
      return null;
    }
  },
  async login(identifier: string, password: string): Promise<User> {
    tokenStore.clear();
    const response = (
      await api.post<BackendLogin>("/auth/login", { identifier, password })
    ).data;
    tokenStore.set(response.accessToken);
    return mapUser(response.user);
  },
  async register(
    name: string,
    email: string,
    password: string,
    username: string,
  ): Promise<User> {
    const response = (
      await api.post<BackendLogin>("/auth/register", {
        name,
        email,
        password,
        username: username || null,
      })
    ).data;
    tokenStore.set(response.accessToken);
    return mapUser(response.user);
  },
  async logout() {
    try {
      await api.post("/auth/logout");
    } catch {
      /* JWT logout is stateless; local cleanup is enough if the token is already invalid. */
    } finally {
      tokenStore.clear();
    }
  },
  async update(user: User): Promise<User> {
    return mapUser(
      (
        await api.put<BackendUser>("/account/profile", {
          name: user.name,
          phone: user.phone || null,
        })
      ).data,
    );
  },
};
