import { api } from "./api";
import type { CartLine } from "../types";
import { type BackendCart, mapCart } from "./backendTypes";

export const cartService = {
  async get(): Promise<CartLine[]> {
    return mapCart((await api.get<BackendCart>("/cart")).data);
  },
  async add(variantId: string, quantity: number): Promise<CartLine[]> {
    return mapCart(
      (
        await api.post<BackendCart>("/cart/items", {
          variantId: Number(variantId),
          quantity,
        })
      ).data,
    );
  },
  async update(itemId: string, quantity: number): Promise<CartLine[]> {
    return mapCart(
      (
        await api.put<BackendCart>(`/cart/items/${encodeURIComponent(itemId)}`, {
          quantity,
        })
      ).data,
    );
  },
  async remove(itemId: string): Promise<CartLine[]> {
    return mapCart(
      (await api.delete<BackendCart>(`/cart/items/${encodeURIComponent(itemId)}`)).data,
    );
  },
  async clear(): Promise<CartLine[]> {
    return mapCart((await api.delete<BackendCart>("/cart")).data);
  },
};
