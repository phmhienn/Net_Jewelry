import { ArrowUpRight, Heart, Plus } from "lucide-react";
import { Link } from "react-router-dom";
import type { Product } from "../../types";
import { money } from "../../utils/format";
import { useStore } from "../../context/StoreContext";
export function ProductCard({ product }: { product: Product }) {
  const { addToCart, wishlist, toggleWishlist } = useStore();
  const liked = wishlist.includes(product.id);
  return (
    <article className="product-card">
      <div className="product-image-wrap">
        <Link to={`/products/${product.id}`} aria-label={`Xem ${product.name}`}>
          <img
            src={product.image}
            alt={product.name}
            loading="lazy"
            width="600"
            height="660"
          />
        </Link>
        {product.badge && <span className="badge">{product.badge}</span>}
        <button
          className={`wishlist-btn icon-btn ${liked ? "selected" : ""}`}
          aria-label={`${liked ? "Bỏ yêu thích" : "Yêu thích"} ${product.name}`}
          aria-pressed={liked}
          onClick={() => toggleWishlist(product.id)}
        >
          <Heart size={19} fill={liked ? "currentColor" : "none"} />
        </button>
      </div>
      <div className="product-meta">
        <span>
          {product.category} · {product.material}
        </span>
        <Link to={`/products/${product.id}`}>
          <h3>{product.name}</h3>
          <ArrowUpRight size={16} />
        </Link>
        <div className="price">
          {money(product.price)}
          {product.originalPrice && <del>{money(product.originalPrice)}</del>}
        </div>
        <button className="add-product" onClick={() => void addToCart(product)}>
          <Plus size={16} />
          Thêm vào giỏ
        </button>
      </div>
    </article>
  );
}
