import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
interface ModelContext {
  registerTool: (
    tool: {
      name: string;
      title: string;
      description: string;
      inputSchema: object;
      annotations: { readOnlyHint: boolean };
      execute: (input: unknown) => Promise<unknown>;
    },
    options: { signal: AbortSignal },
  ) => void | Promise<void>;
}
export function useWebTools() {
  const navigate = useNavigate();
  useEffect(() => {
    const context = (document as Document & { modelContext?: ModelContext })
      .modelContext;
    if (!context) return;
    const lifecycle = new AbortController();
    try {
      void Promise.resolve(
        context.registerTool(
          {
            name: "navigate_product_search",
            title: "Tìm trang sức",
            description:
              "Mở danh sách sản phẩm với từ khóa tìm kiếm. Không thêm giỏ hàng hoặc đặt hàng.",
            inputSchema: {
              type: "object",
              properties: { query: { type: "string", maxLength: 100 } },
              required: ["query"],
              additionalProperties: false,
            },
            annotations: { readOnlyHint: false },
            async execute(input) {
              if (
                typeof input !== "object" ||
                input === null ||
                !("query" in input) ||
                typeof input.query !== "string" ||
                input.query.length > 100
              )
                throw new Error("query phải là chuỗi tối đa 100 ký tự.");
              navigate(
                `/products?search=${encodeURIComponent(input.query.trim())}`,
              );
              await new Promise<void>((resolve) =>
                requestAnimationFrame(() =>
                  requestAnimationFrame(() => resolve()),
                ),
              );
              return { search: input.query.trim(), opened: "products" };
            },
          },
          { signal: lifecycle.signal },
        ),
      ).catch(() => {
        /* Optional capability; visible search remains available. */
      });
    } catch {
      /* Unsupported browsers use visible controls. */
    }
    return () => lifecycle.abort();
  }, [navigate]);
}
