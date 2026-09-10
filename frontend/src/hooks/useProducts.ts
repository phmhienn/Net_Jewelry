import { useEffect, useState } from "react";
import { productService } from "../services/productService";
import type { ProductQuery, ProductResult } from "../types";
import { errorMessage } from "../utils/format";
export function useProducts(query: ProductQuery) {
  const [result, setResult] = useState<ProductResult>({
    items: [],
    total: 0,
    pages: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  const key = JSON.stringify(query);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
    productService
      .list(JSON.parse(key) as ProductQuery, controller.signal)
      .then((data) => {
        if (!controller.signal.aborted) setResult(data);
      })
      .catch((error) => {
        if (!controller.signal.aborted) setError(errorMessage(error));
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [key, version]);
  return {
    ...result,
    loading,
    error,
    retry: () => setVersion((value) => value + 1),
  };
}
