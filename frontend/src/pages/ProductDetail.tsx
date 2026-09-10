import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Star,
  Heart,
  ShoppingBag,
  Truck,
  ShieldCheck,
  Check,
} from "lucide-react";
import type { Product } from "../types";
import { productService } from "../services/productService";
import { isDemo } from "../data/config";
import { useStore } from "../context/StoreContext";
import { money, errorMessage } from "../utils/format";
import { ProductGallery } from "../components/product/ProductGallery";
import { QuantityControl } from "../components/cart/QuantityControl";
import { Button } from "../components/common/Button";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { ErrorState, Loading } from "../components/common/Feedback";
export default function ProductDetail() {
  const { id = "" } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [busy, setBusy] = useState(false);
  const { addToCart, wishlist, toggleWishlist } = useStore();
  const navigate = useNavigate();
  useEffect(() => {
    const controller = new AbortController();
    setProduct(null);
    setError("");
    setQuantity(1);
    productService
      .get(id, controller.signal)
      .then((p) => {
        if (!controller.signal.aborted) setProduct(p);
      })
      .catch((e) => {
        if (!controller.signal.aborted) setError(errorMessage(e));
      });
    return () => controller.abort();
  }, [id, retry]);
  if (error)
    return (
      <div className="container page">
        <ErrorState message={error} retry={() => setRetry((n) => n + 1)} />
      </div>
    );
  if (!product)
    return (
      <div className="container page">
        <Loading count={2} />
      </div>
    );
  const add = async (buy: boolean) => {
    setBusy(true);
    if ((await addToCart(product, quantity)) && buy) navigate("/checkout");
    setBusy(false);
  };
  return (
    <div className="container page">
      <Breadcrumbs
        items={[
          { label: "Sản phẩm", to: "/products" },
          { label: product.name },
        ]}
      />
      <div className="detail-layout">
        <ProductGallery
          key={product.id}
          images={product.images}
          name={product.name}
        />
        <div className="product-detail">
          <div className="eyebrow">
            {product.brand.toUpperCase()} / {product.category.toUpperCase()}
          </div>
          <h1>{product.name}</h1>
          <div className="rating">
            <Star size={16} fill="currentColor" />
            <strong>{product.rating}</strong>
            <span>
              ({product.reviews} đánh giá{isDemo ? " minh họa" : ""})
            </span>
          </div>
          <div className="detail-price">
            {money(product.price)}
            {product.originalPrice && <del>{money(product.originalPrice)}</del>}
          </div>
          <p className="description">{product.description}</p>
          <dl className="specs">
            <div>
              <dt>Chất liệu</dt>
              <dd>{product.material}</dd>
            </div>
            <div>
              <dt>Mã sản phẩm</dt>
              <dd>NET-{product.id}</dd>
            </div>
            <div>
              <dt>Dành cho</dt>
              <dd>{product.gender}</dd>
            </div>
          </dl>
          <div className="stock">
            <Check size={15} />
            {product.stock > 0
              ? `Còn hàng · ${product.stock} sản phẩm`
              : "Tạm hết hàng"}
          </div>
          <div className="quantity-row">
            <span>Số lượng</span>
            <QuantityControl
              value={quantity}
              max={product.stock}
              onChange={setQuantity}
            />
          </div>
          <div className="detail-actions">
            <Button
              loading={busy}
              disabled={!product.stock}
              onClick={() => void add(false)}
            >
              <ShoppingBag size={18} />
              Thêm vào giỏ
            </Button>
            <button
              className={`btn btn-secondary ${wishlist.includes(id) ? "selected" : ""}`}
              aria-label="Yêu thích sản phẩm"
              aria-pressed={wishlist.includes(id)}
              onClick={() => toggleWishlist(id)}
            >
              <Heart
                size={20}
                fill={wishlist.includes(id) ? "currentColor" : "none"}
              />
            </button>
            <Button
              className="buy-now"
              variant="secondary"
              disabled={busy || !product.stock}
              onClick={() => void add(true)}
            >
              Mua ngay
            </Button>
          </div>
          <div className="detail-perks">
            <p>
              <Truck size={18} />
              Miễn phí giao hàng cho đơn từ 1.500.000₫
            </p>
            <p>
              <ShieldCheck size={18} />
              Thông tin chất liệu rõ ràng, hỗ trợ tận tâm
            </p>
          </div>
          <details>
            <summary>Hướng dẫn bảo quản</summary>
            <p>
              Tránh tiếp xúc với hóa chất và nước hoa. Lau nhẹ bằng khăn mềm,
              cất riêng trong hộp khi không sử dụng.
            </p>
          </details>
          <details>
            <summary>Giao hàng & đổi trả</summary>
            <p>
              Dự kiến 2–5 ngày làm việc. Liên hệ cửa hàng khi sản phẩm không
              đúng mô tả hoặc gặp vấn đề khi nhận hàng. Đây là chính sách minh
              họa cho bản demo.
            </p>
          </details>
        </div>
      </div>
    </div>
  );
}
