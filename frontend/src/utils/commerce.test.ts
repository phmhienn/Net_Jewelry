import { describe, it, expect } from "vitest";
import { shipping, subtotal, totalQuantity } from "./format";
import { validateAddress, validEmail } from "./validation";
import type { CartLine } from "../types";

const cart: CartLine[] = [
  {
    product: {
      id: "sp-1",
      name: "Nhẫn bạc",
      category: "Nhẫn",
      brand: "NÉT",
      price: 1200000,
      material: "Bạc",
      image: "",
      images: [],
      description: "",
      stock: 5,
      rating: 0,
      reviews: 0,
    },
    quantity: 2,
  },
  {
    product: {
      id: "sp-2",
      name: "Dây chuyền vàng",
      category: "Dây chuyền",
      brand: "NÉT",
      price: 2500000,
      material: "Vàng 18K",
      image: "",
      images: [],
      description: "",
      stock: 3,
      rating: 0,
      reviews: 0,
    },
    quantity: 1,
  },
];

describe("Giỏ hàng và phí vận chuyển", () => {
  it("tính tiền và số lượng cho nhiều sản phẩm", () => {
    expect(subtotal(cart)).toBe(4900000);
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

describe("Validation checkout", () => {
  const valid = {
    name: "Nguyễn Minh Anh",
    phone: "0901234567",
    street: "123 Nguyễn Trãi",
    city: "TP Hồ Chí Minh",
    district: "Quận 1",
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
