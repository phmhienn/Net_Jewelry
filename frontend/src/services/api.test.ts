import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

describe("REST service contracts", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.stubEnv("VITE_API_BASE_URL", "https://api.example.test");
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllEnvs();
  });

  it("uses API query and lets product request errors surface", async () => {
    const { api } = await import("./api");
    const { productService } = await import("./productService");
    const get = vi.spyOn(api, "get").mockRejectedValue(new Error("Service unavailable"));

    await expect(productService.list({ search: "ring", page: 2 })).rejects.toThrow(
      "Service unavailable",
    );

    expect(get).toHaveBeenCalledWith("/products", {
      params: { keyword: "ring", page: 1, size: 12 },
      signal: undefined,
    });
  });

  it("sends backend cart payload with variant id and quantity", async () => {
    const { api } = await import("./api");
    const { cartService } = await import("./cartService");
    const post = vi.spyOn(api, "post").mockResolvedValue({ data: { data: { items: [] } } });

    await cartService.add("1", 2);

    expect(post).toHaveBeenCalledWith("/cart/items", { variantId: 1, quantity: 2 });
  });

  it("creates an order through the backend order endpoint", async () => {
    const { api } = await import("./api");
    const { orderService } = await import("./orderService");
    const post = vi.spyOn(api, "post").mockResolvedValue({
      data: {
        id: 10,
          code: "DH00010",
          date: "2026-09-10T10:00:00",
          total: 1500000,
          status: "CHO_XAC_NHAN",
          items: [],
          address: {
            id: 2,
            name: "Nguyễn Minh Anh",
            phone: "0901234567",
            street: "1 Nguyễn Trãi",
            district: "Quận 1",
            city: "TP Hồ Chí Minh",
          },
          payment: { method: "COD" },
      },
    });

    const result = await orderService.create(
      {
        address: {
          name: "Nguyễn Minh Anh",
          phone: "0901234567",
          street: "1 Nguyễn Trãi",
          district: "Quận 1",
          city: "TP Hồ Chí Minh",
        },
        payment: "COD",
        note: "Giao giờ hành chính",
      },
      "request-123",
    );

    expect(post).toHaveBeenCalledWith(
      "/orders",
      {
        address: {
          name: "Nguyễn Minh Anh",
          phone: "0901234567",
          city: "TP Hồ Chí Minh",
          district: "Quận 1",
          ward: "",
          street: "1 Nguyễn Trãi",
        },
        payment: "COD",
        note: "Giao giờ hành chính",
        couponCode: null,
      },
      { headers: { "Idempotency-Key": "request-123" } },
    );
    expect(result.code).toBe("DH00010");
  });

  it("treats 401 session lookup as signed out", async () => {
    const { api } = await import("./api");
    const { authService } = await import("./authService");
    vi.spyOn(api, "get").mockRejectedValue({ response: { status: 401 } });
    expect(await authService.me()).toBeNull();
  });
});
