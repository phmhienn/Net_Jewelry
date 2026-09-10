import { ChevronLeft, ChevronRight } from "lucide-react";
export function Pagination({
  page,
  pages,
  onChange,
}: {
  page: number;
  pages: number;
  onChange: (page: number) => void;
}) {
  if (pages < 2) return null;
  return (
    <nav className="pagination" aria-label="Phân trang">
      <button
        className="icon-btn"
        disabled={page <= 1}
        onClick={() => onChange(page - 1)}
        aria-label="Trang trước"
      >
        <ChevronLeft size={18} />
      </button>
      {Array.from({ length: pages }, (_, i) => i + 1).map((value) => (
        <button
          key={value}
          className={`icon-btn ${value === page ? "current" : ""}`}
          aria-label={`Trang ${value}`}
          aria-current={value === page ? "page" : undefined}
          onClick={() => onChange(value)}
        >
          {value}
        </button>
      ))}
      <button
        className="icon-btn"
        disabled={page >= pages}
        onClick={() => onChange(page + 1)}
        aria-label="Trang sau"
      >
        <ChevronRight size={18} />
      </button>
    </nav>
  );
}
