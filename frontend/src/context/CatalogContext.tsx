import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import type { ReactNode } from "react";
import type { BrandItem, CategoryItem, StorefrontSettings } from "../types";
import { productService } from "../services/productService";
import { contentService } from "../services/contentService";
import { errorMessage } from "../utils/format";

interface CatalogContextValue {
  categories: CategoryItem[];
  brands: BrandItem[];
  materials: string[];
  settings: StorefrontSettings;
  loading: boolean;
  error: string;
  reload: () => void;
}

const fallbackSettings: StorefrontSettings = {
  brandName: "NÉT",
  descriptor: "JEWELRY",
  freeShippingThreshold: 1500000,
  shippingFee: 30000,
};

const CatalogContext = createContext<CatalogContextValue | null>(null);

function flatten(items: CategoryItem[]): CategoryItem[] {
  return items.flatMap((item) => [item, ...flatten(item.children ?? [])]);
}

export function CatalogProvider({ children }: { children: ReactNode }) {
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [brands, setBrands] = useState<BrandItem[]>([]);
  const [materials, setMaterials] = useState<string[]>([]);
  const [settings, setSettings] = useState<StorefrontSettings>(fallbackSettings);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
    Promise.allSettled([
      productService.categories(controller.signal),
      productService.brands(controller.signal),
      productService.materials(controller.signal),
      contentService.settings(controller.signal),
    ]).then((results) => {
      if (controller.signal.aborted) return;
      const [categoryResult, brandResult, materialResult, settingResult] = results;
      if (categoryResult.status === "fulfilled") {
        setCategories(flatten(categoryResult.value).filter((item) => !item.status || item.status === "HOAT_DONG"));
      } else setError(errorMessage(categoryResult.reason));
      if (brandResult.status === "fulfilled") setBrands(brandResult.value.filter((item) => !item.status || item.status === "HOAT_DONG"));
      if (materialResult.status === "fulfilled") setMaterials(materialResult.value);
      if (settingResult.status === "fulfilled") setSettings(settingResult.value);
      setLoading(false);
    });
    return () => controller.abort();
  }, [version]);

  const reload = useCallback(() => setVersion((n) => n + 1), []);
  const value = useMemo(
    () => ({ categories, brands, materials, settings, loading, error, reload }),
    [categories, brands, materials, settings, loading, error, reload],
  );

  return <CatalogContext.Provider value={value}>{children}</CatalogContext.Provider>;
}

export function useCatalog() {
  const context = useContext(CatalogContext);
  if (!context) throw new Error("CatalogProvider is required");
  return context;
}
