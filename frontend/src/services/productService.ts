import { api } from "./api";
import { isDemo } from "../data/config";
import { products } from "../data/products";
import type { Product, ProductQuery, ProductResult } from "../types";
export function filterProducts(query: ProductQuery): ProductResult {
  const search = (query.search ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
  const items = products.filter(
    (p) =>
      (!search ||
        `${p.name} ${p.category}`
          .normalize("NFD")
          .replace(/[\u0300-\u036f]/g, "")
          .toLowerCase()
          .includes(search)) &&
      (!query.category || p.category === query.category) &&
      (!query.material || p.material === query.material) &&
      (!query.brand || p.brand === query.brand) &&
      (!query.gender || p.gender === query.gender) &&
      (!query.maxPrice || p.price <= query.maxPrice),
  );
  if (query.sort === "price-asc") items.sort((a, b) => a.price - b.price);
  if (query.sort === "price-desc") items.sort((a, b) => b.price - a.price);
  if (query.sort === "newest") items.sort((a, b) => b.id.localeCompare(a.id));
  const page = Math.max(1, query.page ?? 1);
  return {
    items: items.slice((page - 1) * 8, page * 8),
    total: items.length,
    pages: Math.ceil(items.length / 8),
  };
}
export const productService = {
  async list(
    query: ProductQuery = {},
    signal?: AbortSignal,
  ): Promise<ProductResult> {
    return isDemo
      ? filterProducts(query)
      : (await api.get<ProductResult>("/products", { params: query, signal }))
          .data;
  },
  async get(id: string, signal?: AbortSignal): Promise<Product> {
    if (!isDemo)
      return (
        await api.get<Product>(`/products/${encodeURIComponent(id)}`, {
          signal,
        })
      ).data;
    const product = products.find((p) => p.id === id);
    if (!product)
      throw new Error("Sản phẩm không tồn tại hoặc đã được ngừng bán.");
    return product;
  },
};
