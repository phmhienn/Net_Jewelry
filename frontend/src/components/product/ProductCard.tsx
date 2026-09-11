import { ArrowUpRight, Plus } from "lucide-react";
import { Link } from "react-router-dom";
import type { Product } from "../../types";
import { money } from "../../utils/format";
import { useStore } from "../../context/StoreContext";
import { isCustomer } from "../../utils/access";
import { WishlistButton } from "./WishlistButton";

export function ProductCard({ product }: { product: Product }) {
  const { addToCart, user } = useStore();
  return (
    <article className="product-card">
      <div className="product-image-wrap">
        <Link to={`/products/${product.id}`} aria-label={`Xem ${product.name}`}>
          {product.image ? (
            <img src={product.image} alt={product.name} loading="lazy" width="600" height="660" />
          ) : (
            <span className="product-image-placeholder">Chưa có ảnh</span>
          )}
        </Link>
        {product.badge && <span className="badge">{product.badge}</span>}
      </div>
      <div className="product-meta">
        <span>{[product.category, product.material].filter(Boolean).join(" · ")}</span>
        <Link to={`/products/${product.id}`}>
          <h3>{product.name}</h3>
          <ArrowUpRight size={16} />
        </Link>
        <div className="price">
          {product.price > 0 ? money(product.price) : "Chưa cập nhật giá"}
          {product.originalPrice && <del>{money(product.originalPrice)}</del>}
        </div>
        {(!user || isCustomer(user)) && (
          <button
            disabled={!product.stock || product.price <= 0}
            className="add-product"
            onClick={() => void addToCart(product)}
          >
            <Plus size={16} />
            {product.price <= 0 ? "Chưa thể đặt hàng" : product.stock ? "Thêm vào giỏ" : "Hết hàng"}
          </button>
        )}
        <WishlistButton productId={product.id} />
      </div>
    </article>
  );
}
