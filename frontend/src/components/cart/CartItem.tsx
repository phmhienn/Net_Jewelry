import { Link } from "react-router-dom";
import { Trash2 } from "lucide-react";
import type { CartLine } from "../../types";
import { useStore } from "../../context/StoreContext";
import { money } from "../../utils/format";
import { QuantityControl } from "./QuantityControl";
export function CartItem({ item }: { item: CartLine }) {
  const { setQuantity, cartLoading } = useStore();
  return (
    <article className="cart-item">
      <Link to={`/products/${item.product.id}`} className="cart-item-image">
        <img src={item.product.image} alt={item.product.name} />
      </Link>
      <div className="cart-item-info">
        <Link to={`/products/${item.product.id}`}>
          <h2>{item.product.name}</h2>
        </Link>
        <p>{item.product.material}</p>
        <span>{money(item.product.price)}</span>
        <button
          disabled={cartLoading}
          className="remove-item"
          onClick={() => void setQuantity(item.product.id, 0)}
        >
          <Trash2 size={14} />
          Xóa sản phẩm
        </button>
      </div>
      <QuantityControl
        label={`Số lượng ${item.product.name}`}
        value={item.quantity}
        max={cartLoading ? item.quantity : item.product.stock}
        onChange={(quantity) => void setQuantity(item.product.id, quantity)}
      />
      <strong className="cart-item-total">
        {money(item.product.price * item.quantity)}
      </strong>
    </article>
  );
}
