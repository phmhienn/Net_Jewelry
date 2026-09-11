import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Star, ShoppingBag, Truck, ShieldCheck, Check, X } from "lucide-react";
import type { Product } from "../types";
import { productService } from "../services/productService";
import { useStore } from "../context/StoreContext";
import { useCatalog } from "../context/CatalogContext";
import { isCustomer } from "../utils/access";
import { money, errorMessage } from "../utils/format";
import { ProductGallery } from "../components/product/ProductGallery";
import { QuantityControl } from "../components/cart/QuantityControl";
import { Button } from "../components/common/Button";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { ErrorState, Loading } from "../components/common/Feedback";
import { WishlistButton } from "../components/product/WishlistButton";

export default function ProductDetail() {
  const { id = "" } = useParams();
  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [busy, setBusy] = useState(false);
  const { addToCart, user } = useStore();
  const { settings } = useCatalog();
  const navigate = useNavigate();

  useEffect(() => {
    const controller = new AbortController();
    setProduct(null);
    setError("");
    setQuantity(1);
    productService.get(id, controller.signal)
      .then((p) => { if (!controller.signal.aborted) setProduct(p); })
      .catch((e) => { if (!controller.signal.aborted) setError(errorMessage(e)); });
    return () => controller.abort();
  }, [id, retry]);

  if (error) return <div className="container page"><ErrorState message={error} retry={() => setRetry((n) => n + 1)} /></div>;
  if (!product) return <div className="container page"><Loading count={2} /></div>;

  const add = async (buy: boolean) => {
    setBusy(true);
    if ((await addToCart(product, quantity)) && buy) navigate("/checkout");
    setBusy(false);
  };

  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Sản phẩm", to: "/products" }, { label: product.name }]} />
      <div className="detail-layout">
        <ProductGallery key={product.id} images={product.images} name={product.name} />
        <div className="product-detail">
          <div className="eyebrow">{product.brand.toUpperCase()} / {product.category.toUpperCase()}</div>
          <h1>{product.name}</h1>
          <div className="rating">
            {product.reviews > 0 ? <><Star size={16} fill="currentColor" /><strong>{product.rating.toFixed(1)}</strong><span>({product.reviews} đánh giá)</span></> : <span>Chưa có đánh giá</span>}
          </div>
          <div className="detail-price">{product.price > 0 ? money(product.price) : "Chưa cập nhật giá"}{product.originalPrice && <del>{money(product.originalPrice)}</del>}</div>
          <p className="description">{product.description}</p>
          <dl className="specs">
            <div><dt>Chất liệu</dt><dd>{product.material}</dd></div>
            <div><dt>Mã sản phẩm</dt><dd>{product.sku ?? "Chưa có mã"}</dd></div>
          </dl>
          <div className="purchase-stack">
            <div className={`stock${product.stock === 0 ? " stock-out" : ""}`}>
              {product.stock > 0 ? <Check size={15} /> : <X size={15} />}
              {product.stock > 0 ? `Còn hàng · ${product.stock} sản phẩm` : "Tạm hết hàng"}
            </div>
            {!!product.variants?.length && (
              <label className="field variant-field">
                Kích cỡ / Màu sắc
                <select
                  value={product.variantId}
                  onChange={(e) => {
                    const variant = product.variants!.find((v) => v.id === e.target.value)!;
                    setProduct({ ...product, variantId: variant.id, price: variant.price, originalPrice: variant.originalPrice, stock: variant.available, size: variant.size, color: variant.color });
                    setQuantity(variant.available > 0 ? 1 : 0);
                  }}
                >
                  {product.variants.map((variant) => (
                    <option key={variant.id} value={variant.id}>{[variant.size, variant.color].filter(Boolean).join(" · ") || "Biến thể"}{variant.available ? "" : " — Hết hàng"}</option>
                  ))}
                </select>
              </label>
            )}
          </div>
          {(!user || isCustomer(user)) && <>
            <div className="quantity-row"><span>Số lượng</span><QuantityControl value={quantity} max={product.stock} onChange={setQuantity} /></div>
            <div className="detail-actions">
              <Button loading={busy} disabled={!product.stock || product.price <= 0} onClick={() => void add(false)}><ShoppingBag size={18} />Thêm vào giỏ</Button>
              <Button className="buy-now" variant="secondary" disabled={busy || !product.stock || product.price <= 0} onClick={() => void add(true)}>Mua ngay</Button>
            </div>
          </>}
          <WishlistButton productId={product.id} />
          <div className="detail-perks">
            <p><Truck size={18} />Miễn phí giao hàng cho đơn từ {money(settings.freeShippingThreshold)}</p>
            <p><ShieldCheck size={18} />Thông tin chất liệu rõ ràng, hỗ trợ tận tâm</p>
          </div>
          <details><summary>Hướng dẫn bảo quản</summary><p>Tránh tiếp xúc với hóa chất và nước hoa. Lau nhẹ bằng khăn mềm, cất riêng trong hộp khi không sử dụng.</p></details>
          <details><summary>Giao hàng & đổi trả</summary><p>Thông tin giao hàng được cập nhật theo trạng thái đơn hàng trong tài khoản.</p></details>
        </div>
      </div>
    </div>
  );
}
