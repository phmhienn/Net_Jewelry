import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { orderService } from "../../services/orderService";
import { useStore } from "../../context/StoreContext";
import type { Order } from "../../types";
import { EmptyState, ErrorState } from "../common/Feedback";
import { money, errorMessage } from "../../utils/format";
export function Orders() {
  const { user } = useStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    orderService
      .list(user!.email)
      .then((data) => {
        if (active) setOrders(data);
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
  }, [user, version]);
  return (
    <div className="account-orders">
      <h2>Đơn hàng của bạn</h2>
      {loading ? (
        <p role="status" className="loading-text">
          Đang tải đơn hàng…
        </p>
      ) : error ? (
        <ErrorState message={error} retry={() => setVersion((n) => n + 1)} />
      ) : !orders.length ? (
        <EmptyState
          title="Chưa có đơn hàng"
          description="Những lựa chọn đầu tiên đang chờ bạn."
        >
          <Link to="/products" className="btn btn-primary">
            Khám phá sản phẩm
          </Link>
        </EmptyState>
      ) : (
        <div className="order-list">
          {orders.map((order) => (
            <details key={order.id} className="order-entry">
              <summary>
                <strong>{order.id}</strong>
                <span>{new Date(order.date).toLocaleDateString("vi-VN")}</span>
                <span>{money(order.total)}</span>
                <span className="status-badge">{order.status}</span>
              </summary>
              <div className="order-items">
                {order.items.map((item) => (
                  <div className="summary-row" key={item.product.id}>
                    <span>
                      {item.product.name} × {item.quantity}
                    </span>
                    <strong>{money(item.product.price * item.quantity)}</strong>
                  </div>
                ))}
                <p>
                  Giao đến: {order.address.street}, {order.address.district},{" "}
                  {order.address.city}
                </p>
                <p>
                  Thanh toán:{" "}
                  {order.payment === "cod" ? "Khi nhận hàng" : "Chuyển khoản"}
                </p>
              </div>
            </details>
          ))}
        </div>
      )}
    </div>
  );
}
