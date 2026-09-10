import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
describe("REST service contracts", () => {
  beforeEach(() => {
    vi.resetModules();
    vi.stubEnv("VITE_DEMO_MODE", "false");
    vi.stubEnv("VITE_API_BASE_URL", "https://api.example.test");
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllEnvs();
  });
  it("uses API query and never falls back to demo on a failed product request", async () => {
    const { api } = await import("./api");
    const { productService } = await import("./productService");
    const get = vi
      .spyOn(api, "get")
      .mockRejectedValue(new Error("Service unavailable"));
    await expect(
      productService.list({ search: "ring", page: 2 }),
    ).rejects.toThrow("Service unavailable");
    expect(get).toHaveBeenCalledWith("/products", {
      params: { search: "ring", page: 2 },
      signal: undefined,
    });
  });
  it("sends only product identifiers and quantities when saving cart", async () => {
    const { api } = await import("./api");
    const { cartService } = await import("./cartService");
    const { products } = await import("../data/products");
    const put = vi.spyOn(api, "put").mockResolvedValue({ data: [] });
    await cartService.save([{ product: products[0], quantity: 2 }]);
    expect(put).toHaveBeenCalledWith("/cart", {
      items: [{ productId: "01", quantity: 2 }],
    });
  });
  it("keeps idempotency key and uses server order total", async () => {
    const { api } = await import("./api");
    const { orderService } = await import("./orderService");
    const { products } = await import("../data/products");
    const post = vi
      .spyOn(api, "post")
      .mockResolvedValue({ data: { id: "order-server", total: 1234567 } });
    const input = {
      items: [{ product: products[0], quantity: 1 }],
      email: "demo@example.com",
      address: {
        name: "Demo",
        phone: "0901234567",
        street: "123 Demo",
        city: "Demo",
        district: "Demo",
      },
      payment: "cod",
      note: "",
    };
    const result = await orderService.create(input, "request-123");
    expect(post).toHaveBeenCalledWith(
      "/orders",
      { ...input, items: [{ productId: "01", quantity: 1 }] },
      { headers: { "Idempotency-Key": "request-123" } },
    );
    expect(result.total).toBe(1234567);
  });
  it("treats 401 session lookup as signed out", async () => {
    const { api } = await import("./api");
    const { authService } = await import("./authService");
    vi.spyOn(api, "get").mockResolvedValue({ status: 401, data: null });
    expect(await authService.me()).toBeNull();
  });
});
