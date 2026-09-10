import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useStore } from "../context/StoreContext";
import { productService } from "../services/productService";
import type { Product } from "../types";
import { ProductGrid } from "../components/product/ProductGrid";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { Loading, EmptyState, ErrorState } from "../components/common/Feedback";
import { errorMessage } from "../utils/format";
export default function Wishlist() {
  const { wishlist } = useStore();
  const [items, setItems] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    Promise.all(wishlist.map((id) => productService.get(id)))
      .then((data) => {
        if (active) setItems(data);
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [wishlist, retry]);
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Yêu thích" }]} />
      <h1 className="page-title">Những điều bạn yêu</h1>
      <p className="page-intro">
        Lưu lại một chút tinh tế cho lần ghé thăm tiếp theo.
      </p>
      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} retry={() => setRetry((n) => n + 1)} />
      ) : items.length ? (
        <ProductGrid products={items} />
      ) : (
        <EmptyState
          title="Chưa có sản phẩm yêu thích"
          description="Chạm vào trái tim trên sản phẩm để lưu lại."
        >
          <Link className="btn btn-primary" to="/products">
            Khám phá trang sức
          </Link>
        </EmptyState>
      )}
    </div>
  );
}
