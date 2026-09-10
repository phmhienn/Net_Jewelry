import { useId } from "react";
import type { ProductQuery } from "../../types";
import { categories } from "../../data/config";
import { money } from "../../utils/format";
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
  return (
    <div className="filters">
      <div className="filter-heading">
        <h2>Bộ lọc</h2>
        <button className="text-link" onClick={reset}>
          Xóa tất cả
        </button>
      </div>
      <fieldset>
        <legend>Danh mục</legend>
        {["Tất cả", ...categories].map((category) => (
          <label className="choice" key={category}>
            <input
              type="radio"
              name={`${filterId}-category`}
              checked={
                category === "Tất cả"
                  ? !query.category
                  : query.category === category
              }
              onChange={() =>
                update("category", category === "Tất cả" ? "" : category)
              }
            />
            {category}
          </label>
        ))}
      </fieldset>
      <fieldset>
        <legend>Khoảng giá</legend>
        <label className="range-label" htmlFor={`${filterId}-max-price`}>
          Đến {money(query.maxPrice ?? 3000000)}
        </label>
        <input
          id={`${filterId}-max-price`}
          type="range"
          min="500000"
          max="3000000"
          step="50000"
          value={query.maxPrice ?? 3000000}
          onChange={(e) => update("maxPrice", e.target.value)}
        />
        <div className="range-extents">
          <span>500.000₫</span>
          <span>3.000.000₫</span>
        </div>
      </fieldset>
      {[
        {
          key: "brand",
          label: "Thương hiệu",
          options: ["NÉT Essentials", "NÉT Studio"],
        },
        {
          key: "material",
          label: "Chất liệu",
          options: ["Vàng 18K", "Bạc 925 mạ vàng"],
        },
        { key: "gender", label: "Dành cho", options: ["Nữ", "Unisex"] },
      ].map((group) => (
        <fieldset key={group.key}>
          <legend>{group.label}</legend>
          <select
            aria-label={group.label}
            value={query[group.key as keyof ProductQuery] ?? ""}
            onChange={(e) => update(group.key, e.target.value)}
          >
            <option value="">Tất cả</option>
            {group.options.map((option) => (
              <option key={option}>{option}</option>
            ))}
          </select>
        </fieldset>
      ))}
    </div>
  );
}
