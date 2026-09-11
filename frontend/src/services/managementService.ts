import { api } from "./api";
import type { PageResult } from "../types";
import { type BackendPage } from "./backendTypes";

export type Row = Record<string, unknown> & { id?: string | number };

const idRow = <T extends Row>(row: T): T => ({ ...row, id: String(row.id ?? "") });

export interface Dashboard {
  totalOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  shippingOrders: number;
  products: number;
  customers: number;
  revenue: number;
}

export const managementService = {
  async page<T extends Row>(path: string, params: Record<string, unknown> = {}): Promise<PageResult<T>> {
    const page = (await api.get<BackendPage<T>>(path, { params })).data;
    return {
      content: page.content.map(idRow),
      page: page.page,
      size: page.size,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
    };
  },
  async list<T extends Row>(path: string, params: Record<string, unknown> = {}): Promise<T[]> {
    return (await api.get<T[]>(path, { params })).data.map(idRow);
  },
  async get<T extends Row>(path: string): Promise<T> {
    return idRow((await api.get<T>(path)).data);
  },
  async create<T>(path: string, body: unknown): Promise<T> {
    return (await api.post<T>(path, body)).data;
  },
  async update<T>(path: string, body: unknown): Promise<T> {
    return (await api.put<T>(path, body)).data;
  },
  async patch<T>(path: string, body: unknown): Promise<T> {
    return (await api.patch<T>(path, body)).data;
  },
  async remove(path: string): Promise<void> {
    await api.delete(path);
  },
  async dashboard(params: Record<string, unknown> = {}): Promise<Dashboard> {
    const data = (await api.get<Dashboard>("/admin/statistics/dashboard", { params })).data;
    return { ...data, revenue: Number(data.revenue ?? 0) };
  },
};
