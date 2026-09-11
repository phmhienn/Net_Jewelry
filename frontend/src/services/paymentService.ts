import { api } from "./api";
import type { PaymentStatus } from "../types";
import { asId, asNumber } from "./backendTypes";

interface BackendPaymentStatus {
  orderId: number;
  orderCode: string;
  paymentStatus: string;
  paymentMethod: string;
  amount: number | string;
}

export const paymentService = {
  async status(orderId: string): Promise<PaymentStatus> {
    const data = (await api.get<BackendPaymentStatus>(`/orders/${encodeURIComponent(orderId)}/payment-status`)).data;
    return {
      orderId: asId(data.orderId),
      orderCode: data.orderCode,
      paymentStatus: data.paymentStatus,
      paymentMethod: data.paymentMethod,
      amount: asNumber(data.amount),
    };
  },
};
