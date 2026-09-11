import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { Product } from "../types";
import { wishlistService } from "../services/wishlistService";
import { ProductGrid } from "../components/product/ProductGrid";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { Loading, EmptyState, ErrorState } from "../components/common/Feedback";
import { errorMessage } from "../utils/format";

export default function Wishlist() {
  const [items, setItems] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError("");
    wishlistService.list(controller.signal)
      .then(setItems)
      .catch((e) => { if (!controller.signal.aborted) setError(errorMessage(e)); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [retry]);
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Yêu thích" }]} />
      <h1 className="page-title">Sản phẩm yêu thích</h1>
      <p className="page-intro">Danh sách sản phẩm bạn đã lưu trong tài khoản.</p>
      {loading ? <Loading /> : error ? <ErrorState message={error} retry={() => setRetry((n) => n + 1)} /> : items.length ? <ProductGrid products={items} /> : (
        <EmptyState title="Chưa có sản phẩm yêu thích" description="Chạm vào nút yêu thích trên sản phẩm để lưu lại."><Link className="btn btn-primary" to="/products">Khám phá trang sức</Link></EmptyState>
      )}
    </div>
  );
}
