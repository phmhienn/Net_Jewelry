import type { Address } from "../types";
const safeText = (value: unknown) => String(value ?? "");

export const validEmail = (value: string) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(safeText(value).trim());
export function validateAddress(address: Address) {
  const errors: Record<string, string> = {};
  const name = safeText(address.name).trim();
  const phone = safeText(address.phone).replace(/[\s.-]/g, "");
  const street = safeText(address.street).trim();
  const city = safeText(address.city).trim();
  const district = safeText(address.district).trim();
  const ward = safeText(address.ward).trim();
  if (name.length < 2)
    errors.name = "Vui lòng nhập họ tên từ 2 ký tự.";
  if (!/^(0\d{9}|\+84\d{9})$/.test(phone))
    errors.phone = "Nhập số điện thoại Việt Nam hợp lệ (10 chữ số).";
  if (street.length < 5)
    errors.street = "Nhập số nhà và tên đường (ít nhất 5 ký tự).";
  if (!city) errors.city = "Vui lòng nhập tỉnh/thành phố.";
  if (!district)
    errors.district = "Vui lòng nhập quận/huyện.";
  if (!ward) errors.ward = "Vui lòng nhập phường/xã.";
  return errors;
}
