import { api } from "./api";
import type { Address, Order } from "../types";
import { type BackendOrder, type BackendPage, mapOrder } from "./backendTypes";

export interface CreateOrder {
  address: Address;
  payment: string;
  note: string;
  couponCode?: string;
}

export const orderService = {
  async create(input: CreateOrder, requestId: string): Promise<Order> {
    return mapOrder(
      (
        await api.post<BackendOrder>(
          "/orders",
          {
            address: {
              name: input.address.name,
              phone: input.address.phone,
              city: input.address.city,
              district: input.address.district,
              ward: input.address.ward || "",
              street: input.address.street,
            },
            payment: input.payment,
            note: input.note || null,
            couponCode: input.couponCode || null,
          },
          { headers: { "Idempotency-Key": requestId } },
        )
      ).data,
    );
  },
  async get(orderId: string): Promise<Order> {
    return mapOrder((await api.get<BackendOrder>(`/orders/${encodeURIComponent(orderId)}`)).data);
  },
  async list(): Promise<Order[]> {
    const page = (
      await api.get<BackendPage<BackendOrder>>("/orders", {
        params: { page: 0, size: 50 },
      })
    ).data;
    return page.content.map(mapOrder);
  },
  async cancel(id: string): Promise<Order> {
    return mapOrder((await api.post<BackendOrder>(`/orders/${encodeURIComponent(id)}/cancel`)).data);
  },
};
