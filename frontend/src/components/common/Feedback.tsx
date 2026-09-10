import { PackageOpen, RefreshCw } from "lucide-react";
import { Button } from "./Button";
export function EmptyState({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children?: React.ReactNode;
}) {
  return (
    <div className="empty-state">
      <PackageOpen size={38} strokeWidth={1.2} />
      <h2>{title}</h2>
      {description && <p>{description}</p>}
      {children}
    </div>
  );
}
export function ErrorState({
  message,
  retry,
}: {
  message: string;
  retry: () => void;
}) {
  return (
    <div className="empty-state" role="alert">
      <h2>Không thể tải dữ liệu</h2>
      <p>{message}</p>
      <Button variant="secondary" onClick={retry}>
        <RefreshCw size={16} />
        Thử lại
      </Button>
    </div>
  );
}
export function Loading({ count = 4 }: { count?: number }) {
  return (
    <div className="product-grid" role="status" aria-label="Đang tải sản phẩm">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="skeleton-card">
          <div className="skeleton-image" />
          <div className="skeleton-line" />
          <div className="skeleton-line short" />
        </div>
      ))}
    </div>
  );
}
