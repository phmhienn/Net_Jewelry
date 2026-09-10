import { api } from "./api";
import { isDemo } from "../data/config";
import type { CartLine } from "../types";
export const cartService = {
  async get(): Promise<CartLine[]> {
    return (await api.get<CartLine[]>("/cart")).data;
  },
  async save(items: CartLine[]): Promise<CartLine[]> {
    if (isDemo) return items;
    return (
      await api.put<CartLine[]>("/cart", {
        items: items.map((item) => ({
          productId: item.product.id,
          quantity: item.quantity,
        })),
      })
    ).data;
  },
};
