import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { SlidersHorizontal, Search, X } from "lucide-react";
import { useProducts } from "../hooks/useProducts";
import { ProductGrid } from "../components/product/ProductGrid";
import { ProductFilter } from "../components/product/ProductFilter";
import { Pagination } from "../components/common/Pagination";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { Modal } from "../components/common/Modal";
import { Loading, EmptyState, ErrorState } from "../components/common/Feedback";
import { Button } from "../components/common/Button";
import { useCatalog } from "../context/CatalogContext";
import type { ProductQuery } from "../types";

export default function Products() {
  const [params, setParams] = useSearchParams();
  const [filterOpen, setFilterOpen] = useState(false);
  const [search, setSearch] = useState(params.get("search") ?? "");
  const { categories } = useCatalog();
  useEffect(() => setSearch(params.get("search") ?? ""), [params]);
  const legacyCategoryName = params.get("category") ?? "";
  const selectedCategoryId =
    params.get("categoryId") ??
    categories.find((category) => category.name === legacyCategoryName)?.id ??
    "";
  const selectedCategoryName =
    categories.find((category) => category.id === selectedCategoryId)?.name ??
    legacyCategoryName;
  const query: ProductQuery = {
    search: params.get("search") ?? "",
    categoryId: selectedCategoryId,
    material: params.get("material") ?? "",
    brandId: params.get("brandId") ?? "",
    maxPrice: Number(params.get("maxPrice")) || undefined,
    sort: params.get("sort") ?? "",
    page: Math.max(1, Number(params.get("page")) || 1),
    size: 12,
  };
  const { items, total, pages, loading, error, retry } = useProducts(query);
  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    if (key !== "page") next.delete("page");
    setParams(next);
  };
  const reset = () => { setParams({}); setSearch(""); };
  const filter = <ProductFilter query={query} update={update} reset={reset} />;
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Sản phẩm" }]} />
      <h1 className="page-title">{selectedCategoryName || "Tất cả trang sức"}</h1>
      <p className="page-intro">Tìm kiếm, lọc và sắp xếp sản phẩm theo ý muốn của bạn.</p>
      <div className="catalog-layout">
        <aside className="desktop-filter">{filter}</aside>
        <div className="catalog-main">
          <div className="catalog-toolbar">
            <span>{loading ? "Đang tải sản phẩm…" : `${total} sản phẩm`}</span>
            <Button variant="secondary" className="filter-toggle" onClick={() => setFilterOpen(true)}><SlidersHorizontal size={16} />Bộ lọc</Button>
            <select aria-label="Sắp xếp sản phẩm" value={query.sort} onChange={(e) => update("sort", e.target.value)}>
              <option value="">Mặc định</option>
              <option value="newest">Mới nhất</option>
              <option value="price-asc">Giá: thấp đến cao</option>
              <option value="price-desc">Giá: cao đến thấp</option>
            </select>
          </div>
          <form className="catalog-search" onSubmit={(e) => { e.preventDefault(); update("search", search.trim()); }}>
            <Search size={18} />
            <input aria-label="Tìm trong sản phẩm" placeholder="Tìm theo tên hoặc mã sản phẩm…" value={search} onChange={(e) => setSearch(e.target.value)} />
            <button className="btn btn-ghost">Tìm kiếm</button>
          </form>
          {query.search && <button className="chip active-filter" onClick={() => { update("search", ""); setSearch(""); }}>“{query.search}” <X size={14} /></button>}
          {loading ? <Loading count={8} /> : error ? <ErrorState message={error} retry={retry} /> : items.length ? (
            <>
              <ProductGrid products={items} />
              <Pagination page={query.page ?? 1} pages={pages} onChange={(page) => { update("page", String(page)); window.scrollTo({ top: 0 }); }} />
            </>
          ) : (
            <EmptyState title="Không tìm thấy sản phẩm" description="Thử tìm từ khóa khác hoặc xoá bộ lọc để xem thêm sản phẩm.">
              <Button variant="secondary" onClick={reset}>Xóa bộ lọc</Button>
            </EmptyState>
          )}
        </div>
      </div>
      <Modal open={filterOpen} onClose={() => setFilterOpen(false)} title="Lọc sản phẩm" drawer>
        {filterOpen && filter}
        <Button className="w-full mt-6" onClick={() => setFilterOpen(false)}>Xem {total} sản phẩm</Button>
      </Modal>
    </div>
  );
}
