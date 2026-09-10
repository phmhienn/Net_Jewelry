import type { CartLine } from "../types";
export const money = (value: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    value,
  );
export const subtotal = (items: CartLine[]) =>
  items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
export const shipping = (total: number) =>
  total === 0 || total >= 1500000 ? 0 : 30000;
export const totalQuantity = (items: CartLine[]) =>
  items.reduce((sum, item) => sum + item.quantity, 0);
export const errorMessage = (error: unknown) =>
  error instanceof Error ? error.message : "Có lỗi xảy ra. Vui lòng thử lại.";
