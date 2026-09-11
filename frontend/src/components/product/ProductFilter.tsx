import { useId } from "react";
import type { ProductQuery } from "../../types";
import { money } from "../../utils/format";
import { useCatalog } from "../../context/CatalogContext";

export function ProductFilter({
  query,
  update,
  reset,
}: {
  query: ProductQuery;
  update: (key: string, value: string) => void;
  reset: () => void;
}) {
  const filterId = useId();
  const { categories, brands, materials } = useCatalog();
  return (
    <div className="filters">
      <div className="filter-heading">
        <h2>Bộ lọc</h2>
        <button className="text-link" onClick={reset} type="button">Xóa tất cả</button>
      </div>
      <fieldset>
        <legend>Danh mục</legend>
        <label className="choice">
          <input
            type="radio"
            name={`${filterId}-category`}
            checked={!query.categoryId}
            onChange={() => update("categoryId", "")}
          />
          Tất cả
        </label>
        {categories.map((category) => (
          <label className="choice" key={category.id}>
            <input
              type="radio"
              name={`${filterId}-category`}
              checked={query.categoryId === category.id}
              onChange={() => update("categoryId", category.id)}
            />
            {category.name}
          </label>
        ))}
      </fieldset>
      <fieldset>
        <legend>Khoảng giá</legend>
        <label className="range-label" htmlFor={`${filterId}-max-price`}>
          Đến {money(query.maxPrice ?? 20000000)}
        </label>
        <input
          id={`${filterId}-max-price`}
          type="range"
          min="500000"
          max="20000000"
          step="100000"
          value={query.maxPrice ?? 20000000}
          onChange={(e) => update("maxPrice", e.target.value)}
        />
        <div className="range-extents">
          <span>500.000₫</span>
          <span>20.000.000₫</span>
        </div>
      </fieldset>
      <fieldset>
        <legend>Thương hiệu</legend>
        <select aria-label="Thương hiệu" value={query.brandId ?? ""} onChange={(e) => update("brandId", e.target.value)}>
          <option value="">Tất cả</option>
          {brands.map((brand) => <option key={brand.id} value={brand.id}>{brand.name}</option>)}
        </select>
      </fieldset>
      <fieldset>
        <legend>Chất liệu</legend>
        <select aria-label="Chất liệu" value={query.material ?? ""} onChange={(e) => update("material", e.target.value)}>
          <option value="">Tất cả</option>
          {materials.map((material) => <option key={material}>{material}</option>)}
        </select>
      </fieldset>
    </div>
  );
}
