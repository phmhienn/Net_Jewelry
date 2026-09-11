import type { UserRole } from "../types";

export const roleLabels: Record<UserRole, string> = {
  KHACH_HANG: "Khách hàng",
  NHAN_VIEN: "Nhân viên",
  QUAN_LY: "Quản lý",
};

export const isCustomer = (user: { role?: UserRole } | null | undefined) =>
  user?.role === "KHACH_HANG";

export const isStaff = (user: { role?: UserRole } | null | undefined) =>
  user?.role === "NHAN_VIEN" || user?.role === "QUAN_LY";

export const isManager = (user: { role?: UserRole } | null | undefined) =>
  user?.role === "QUAN_LY";

export function accountTabs(role: UserRole) {
  if (role === "KHACH_HANG") return ["profile", "orders", "addresses"];
  if (role === "NHAN_VIEN") return ["profile", "manage-orders"];
  return ["profile", "manage-orders", "statistics"];
}
