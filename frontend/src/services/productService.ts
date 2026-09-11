import { api } from "./api";
import type { Product, ProductQuery, ProductResult } from "../types";
import {
  type BackendBrand,
  type BackendCategory,
  type BackendPage,
  type BackendProduct,
  mapBrand,
  mapCategory,
  mapProduct,
} from "./backendTypes";

function sortValue(sort?: string) {
  if (sort === "price-asc") return "priceAsc";
  if (sort === "price-desc") return "priceDesc";
  return sort || undefined;
}

function params(query: ProductQuery) {
  return Object.fromEntries(
    Object.entries({
      keyword: query.search || undefined,
      category: query.categoryId || undefined,
      brand: query.brandId || undefined,
      material: query.material || undefined,
      minPrice: query.minPrice,
      maxPrice: query.maxPrice,
      sort: sortValue(query.sort),
      page: Math.max(0, (query.page ?? 1) - 1),
      size: query.size ?? 12,
    }).filter(([, value]) => value !== undefined && value !== ""),
  );
}

export const productService = {
  async list(
    query: ProductQuery = {},
    signal?: AbortSignal,
  ): Promise<ProductResult> {
    const page = (
      await api.get<BackendPage<BackendProduct>>("/products", {
        params: params(query),
        signal,
      })
    ).data;
    return {
      items: page.content.map(mapProduct),
      total: page.totalElements,
      pages: page.totalPages,
      page: page.page + 1,
    };
  },
  async get(id: string, signal?: AbortSignal): Promise<Product> {
    return mapProduct(
      (
        await api.get<BackendProduct>(`/products/${encodeURIComponent(id)}`, {
          signal,
        })
      ).data,
    );
  },
  async categories(signal?: AbortSignal) {
    return (
      await api.get<BackendCategory[]>("/categories", { signal })
    ).data.map(mapCategory);
  },
  async brands(signal?: AbortSignal) {
    return (await api.get<BackendBrand[]>("/brands", { signal })).data.map(mapBrand);
  },
  async materials(signal?: AbortSignal) {
    return (await api.get<string[]>("/products/materials", { signal })).data;
  },
};
