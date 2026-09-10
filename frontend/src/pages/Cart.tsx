import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight } from "lucide-react";
import { useStore } from "../context/StoreContext";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { EmptyState, ErrorState, Loading } from "../components/common/Feedback";
import { CartItem } from "../components/cart/CartItem";
import { CartSummary } from "../components/cart/CartSummary";
import { totalQuantity } from "../utils/format";
export default function Cart() {
  const { cart, cartLoading, cartError, reloadCart } = useStore();
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Giỏ hàng" }]} />
      <h1 className="page-title">
        Giỏ hàng của bạn{" "}
        <span className="title-count">({totalQuantity(cart)})</span>
      </h1>
      <p className="page-intro">Những lựa chọn nhỏ, mang dấu ấn riêng.</p>
      {cartError ? (
        <ErrorState message={cartError} retry={reloadCart} />
      ) : cartLoading && !cart.length ? (
        <Loading count={2} />
      ) : !cart.length ? (
        <EmptyState
          title="Giỏ hàng đang trống"
          description="Một món trang sức bạn yêu đang chờ được khám phá."
        >
          <Link className="btn btn-primary" to="/products">
            Tiếp tục mua sắm <ArrowRight size={17} />
          </Link>
        </EmptyState>
      ) : (
        <div className="cart-layout">
          <div aria-busy={cartLoading}>
            <div className="cart-labels">
              <span>Sản phẩm</span>
              <span>Số lượng</span>
              <span>Thành tiền</span>
            </div>
            {cart.map((item) => (
              <CartItem key={item.product.id} item={item} />
            ))}
            <Link className="text-link mt-6" to="/products">
              <ArrowLeft size={17} />
              Tiếp tục mua sắm
            </Link>
          </div>
          <CartSummary items={cart}>
            <Link className="btn btn-primary w-full" to="/checkout">
              Tiến hành thanh toán <ArrowRight size={17} />
            </Link>
          </CartSummary>
        </div>
      )}
    </div>
  );
}
