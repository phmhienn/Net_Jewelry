import { api } from "./api";
import { isDemo } from "../data/config";
import type { Address, CartLine, Order } from "../types";
import { subtotal, shipping } from "../utils/format";
import { readLocal, writeLocal } from "../utils/storage";
export interface CreateOrder {
  items: CartLine[];
  address: Address;
  email: string;
  payment: string;
  note: string;
}
export const orderService = {
  async create(input: CreateOrder, requestId: string): Promise<Order> {
    if (!isDemo)
      return (
        await api.post<Order>(
          "/orders",
          {
            ...input,
            items: input.items.map((item) => ({
              productId: item.product.id,
              quantity: item.quantity,
            })),
          },
          { headers: { "Idempotency-Key": requestId } },
        )
      ).data;
    if (!input.items.length) throw new Error("Giỏ hàng đang trống.");
    const existing = readLocal<Order[]>(
      `net-orders:${input.email.toLowerCase()}`,
      [],
    );
    const previous = existing.find(
      (order) => order.id === `NET-${requestId.slice(0, 8).toUpperCase()}`,
    );
    if (previous) return previous;
    const total = subtotal(input.items);
    const order: Order = {
      id: `NET-${requestId.slice(0, 8).toUpperCase()}`,
      items: input.items,
      address: input.address,
      payment: input.payment,
      total: total + shipping(total),
      status: "Đã tiếp nhận (demo)",
      date: new Date().toISOString(),
    };
    writeLocal(`net-orders:${input.email.toLowerCase()}`, [order, ...existing]);
    return order;
  },
  async list(email: string): Promise<Order[]> {
    return isDemo
      ? readLocal<Order[]>(`net-orders:${email.toLowerCase()}`, [])
      : (await api.get<Order[]>("/orders")).data;
  },
};
