import type { Address } from "../types";
export const validEmail = (value: string) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
export function validateAddress(address: Address) {
  const errors: Record<string, string> = {};
  if (address.name.trim().length < 2)
    errors.name = "Vui lòng nhập họ tên từ 2 ký tự.";
  if (!/^(0\d{9}|\+84\d{9})$/.test(address.phone.replace(/[\s.-]/g, "")))
    errors.phone = "Nhập số điện thoại Việt Nam hợp lệ (10 chữ số).";
  if (address.street.trim().length < 5)
    errors.street = "Nhập số nhà và tên đường (ít nhất 5 ký tự).";
  if (!address.city.trim()) errors.city = "Vui lòng nhập tỉnh/thành phố.";
  if (!address.district.trim())
    errors.district = "Vui lòng nhập quận/huyện.";
  if (!address.ward?.trim()) errors.ward = "Vui lòng nhập phường/xã.";
  return errors;
}
