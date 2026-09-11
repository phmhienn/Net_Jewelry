import { api } from "./api";
import type { Banner, StorefrontSettings } from "../types";
import {
  type BackendBanner,
  type BackendPage,
  type BackendSettings,
  mapBanner,
  mapSettings,
} from "./backendTypes";

export interface ContentPage {
  id: string;
  type: string;
  title: string;
  slug: string;
  content: string;
  status?: string;
}

export const contentService = {
  async settings(signal?: AbortSignal): Promise<StorefrontSettings> {
    return mapSettings(
      (await api.get<BackendSettings>("/storefront/settings", { signal })).data,
    );
  },
  async banners(signal?: AbortSignal): Promise<Banner[]> {
    const page = (await api.get<BackendPage<BackendBanner>>("/banners", { params: { page: 0, size: 20 }, signal })).data;
    return page.content.map(mapBanner);
  },
  async contents(signal?: AbortSignal): Promise<ContentPage[]> {
    const page = (await api.get<BackendPage<ContentPage>>("/contents", { params: { page: 0, size: 50 }, signal })).data;
    return page.content.map((item) => ({ ...item, id: String(item.id) }));
  },
  async content(slug: string, signal?: AbortSignal): Promise<ContentPage> {
    const item = (await api.get<ContentPage>(`/contents/${encodeURIComponent(slug)}`, { signal })).data;
    return { ...item, id: String(item.id) };
  },
};
