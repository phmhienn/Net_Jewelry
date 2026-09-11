import { api } from "./api";
import type { Product } from "../types";
import { type BackendPage, type BackendProduct, mapProduct } from "./backendTypes";

export const wishlistService = {
  async list(signal?: AbortSignal): Promise<Product[]> {
    const page = (
      await api.get<BackendPage<BackendProduct>>("/wishlist", {
        params: { page: 0, size: 100 },
        signal,
      })
    ).data;
    return page.content.map(mapProduct);
  },
  async has(productId: string) {
    return (await api.get<boolean>(`/wishlist/${encodeURIComponent(productId)}`)).data;
  },
  async add(productId: string) {
    await api.put(`/wishlist/${encodeURIComponent(productId)}`);
  },
  async remove(productId: string) {
    await api.delete(`/wishlist/${encodeURIComponent(productId)}`);
  },
};
