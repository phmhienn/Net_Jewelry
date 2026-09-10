import { describe, it, expect } from "vitest";
import { subtotal, shipping, totalQuantity } from "./format";
import { validateAddress, validEmail } from "./validation";
import { filterProducts } from "../services/productService";
import { products } from "../data/products";
describe("Giỏ hàng và phí vận chuyển", () => {
  it("tính tiền và số lượng cho nhiều sản phẩm", () => {
    const cart = [
      { product: products[0], quantity: 2 },
      { product: products[1], quantity: 1 },
    ];
    expect(subtotal(cart)).toBe(4350000);
    expect(totalQuantity(cart)).toBe(3);
  });
  it("không thu phí khi giỏ trống", () => {
    expect(subtotal([])).toBe(0);
    expect(shipping(0)).toBe(0);
  });
  it("áp dụng chính xác ngưỡng miễn phí", () => {
    expect(shipping(1499999)).toBe(30000);
    expect(shipping(1500000)).toBe(0);
  });
});
describe("Catalog", () => {
  it("tìm kiếm tiếng Việt không dấu", () => {
    const result = filterProducts({ search: "nhan" });
    expect(result.total).toBe(3);
    expect(result.items.every((p) => p.category === "Nhẫn")).toBe(true);
  });
  it("kết hợp bộ lọc và giá tăng dần", () => {
    const result = filterProducts({
      category: "Vòng cổ",
      material: "Vàng 18K",
      maxPrice: 2300000,
      sort: "price-asc",
    });
    expect(result.items.map((p) => p.price)).toEqual([1850000, 2250000]);
  });
  it("phân trang không lặp sản phẩm", () => {
    const first = filterProducts({ page: 1 });
    const second = filterProducts({ page: 2 });
    expect(first.items).toHaveLength(8);
    expect(second.items).toHaveLength(4);
    expect(
      new Set([...first.items, ...second.items].map((p) => p.id)).size,
    ).toBe(12);
  });
  it("trả về trạng thái trống cho bộ lọc không khớp", () => {
    expect(filterProducts({ search: "no-match-product" })).toEqual({
      items: [],
      total: 0,
      pages: 0,
    });
  });
});
describe("Validation checkout", () => {
  const valid = {
    name: "Khách Demo",
    phone: "0901234567",
    street: "123 Đường Mẫu",
    city: "TP Hồ Chí Minh",
    district: "Phường Mẫu",
  };
  it("chấp nhận địa chỉ hợp lệ", () => {
    expect(validateAddress(valid)).toEqual({});
  });
  it("chấp nhận số quốc tế Việt Nam", () => {
    expect(validateAddress({ ...valid, phone: "+84 901 234 567" })).toEqual({});
  });
  it("báo từng trường thiếu và số điện thoại sai", () => {
    expect(
      Object.keys(
        validateAddress({
          name: " ",
          phone: "abc",
          street: "",
          city: "",
          district: "",
        }),
      ),
    ).toHaveLength(5);
  });
  it("từ chối email sai", () => {
    expect(validEmail("name@domain")).toBe(false);
    expect(validEmail("hello@example.com")).toBe(true);
  });
});
