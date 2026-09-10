import { Link } from "react-router-dom";
import { ShoppingBag, Trash2 } from "lucide-react";
import { useStore } from "../../context/StoreContext";
import { Modal } from "../common/Modal";
import { EmptyState, ErrorState } from "../common/Feedback";
import { QuantityControl } from "./QuantityControl";
import { money, subtotal } from "../../utils/format";
export function CartDrawer() {
  const {
    cartOpen,
    setCartOpen,
    cart,
    cartLoading,
    cartError,
    reloadCart,
    setQuantity,
  } = useStore();
  return (
    <Modal
      open={cartOpen}
      onClose={() => setCartOpen(false)}
      title="Giỏ hàng của bạn"
      drawer
    >
      {cartError ? (
        <ErrorState message={cartError} retry={reloadCart} />
      ) : cartLoading && !cart.length ? (
        <p role="status">Đang tải giỏ hàng…</p>
      ) : cart.length ? (
        <>
          <div className="drawer-items" aria-busy={cartLoading}>
            {cart.map((item) => (
              <div className="mini-cart-item" key={item.product.id}>
                <img src={item.product.image} alt={item.product.name} />
                <div className="grow">
                  <Link
                    to={`/products/${item.product.id}`}
                    onClick={() => setCartOpen(false)}
                  >
                    <h3>{item.product.name}</h3>
                  </Link>
                  <p>{money(item.product.price)}</p>
                  <div className="drawer-quantity">
                    <QuantityControl
                      label={`Số lượng ${item.product.name}`}
                      value={item.quantity}
                      max={item.product.stock}
                      onChange={(quantity) =>
                        void setQuantity(item.product.id, quantity)
                      }
                    />
                    <button
                      disabled={cartLoading}
                      className="icon-btn"
                      aria-label={`Xóa ${item.product.name}`}
                      onClick={() => void setQuantity(item.product.id, 0)}
                    >
                      <Trash2 size={17} />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <div className="summary-row">
            <span>Tạm tính</span>
            <strong>{money(subtotal(cart))}</strong>
          </div>
          <Link
            className="btn btn-primary w-full mt-4"
            to="/cart"
            onClick={() => setCartOpen(false)}
          >
            Xem giỏ hàng <ShoppingBag size={17} />
          </Link>
        </>
      ) : (
        <EmptyState
          title="Giỏ hàng đang trống"
          description="Tìm một thiết kế dành riêng cho bạn."
        >
          <Link
            className="btn btn-primary"
            to="/products"
            onClick={() => setCartOpen(false)}
          >
            Khám phá sản phẩm
          </Link>
        </EmptyState>
      )}
    </Modal>
  );
}
