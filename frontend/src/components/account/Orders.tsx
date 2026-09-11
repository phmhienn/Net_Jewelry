import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { Package, Clock, XCircle, FileText, CheckCircle2, Truck } from "lucide-react";
import { orderService } from "../../services/orderService";
import type { Order } from "../../types";
import { ErrorState } from "../common/Feedback";
import { money, errorMessage } from "../../utils/format";
import { Button } from "../common/Button";

function statusInfo(status: string) {
  switch (status) {
    case "CHO_XAC_NHAN": return { label: "Chờ xác nhận", color: "warning", icon: Clock };
    case "DA_XAC_NHAN": return { label: "Đã xác nhận", color: "info", icon: FileText };
    case "DANG_XU_LY": return { label: "Đang xử lý", color: "info", icon: Package };
    case "DANG_GIAO_HANG": return { label: "Đang giao hàng", color: "primary", icon: Truck };
    case "HOAN_THANH": return { label: "Hoàn thành", color: "success", icon: CheckCircle2 };
    case "DA_HUY": return { label: "Đã huỷ", color: "danger", icon: XCircle };
    default: return { label: status, color: "neutral", icon: Package };
  }
}

export function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  const [cancelling, setCancelling] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true); setError("");
    orderService.list()
      .then((data) => { if (active) setOrders(data); })
      .catch((e) => { if (active) setError(errorMessage(e)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [version]);

  const handleCancel = async (id: string) => {
    if (!confirm("Bạn có chắc muốn huỷ đơn hàng này không?")) return;
    setCancelling(id);
    try {
      await orderService.cancel(id);
      setVersion((n) => n + 1);
    } catch (e) {
      alert(errorMessage(e));
    } finally {
      setCancelling(null);
    }
  };

  return (
    <div className="account-orders">
      <div className="orders-page-header">
        <h2>Đơn hàng của bạn</h2>
        <p className="muted">Theo dõi và quản lý các đơn đặt hàng.</p>
      </div>

      {loading ? (
        <div className="orders-loading" role="status">
          <div className="order-skeleton" />
          <div className="order-skeleton" />
        </div>
      ) : error ? (
        <ErrorState message={error} retry={() => setVersion((n) => n + 1)} />
      ) : !orders.length ? (
        <div className="orders-empty">
          <Package size={36} strokeWidth={1.5} />
          <p>Bạn chưa có đơn đặt hàng nào.</p>
          <Link to="/products" className="btn btn-primary">Khám phá sản phẩm</Link>
        </div>
      ) : (
        <div className="my-orders-list">
          {orders.map((order) => {
            const status = statusInfo(order.status);
            const StatusIcon = status.icon;
            
            return (
              <div key={order.id} className="my-order-card">
                <div className="my-order-header">
                  <div className="my-order-meta">
                    <strong>Mã đơn: #{order.code || order.id}</strong>
                    <span className="my-order-date">Đặt ngày {new Date(order.date).toLocaleDateString("vi-VN")}</span>
                  </div>
                  <div className={`my-order-status badge-${status.color}`}>
                    <StatusIcon size={14} /> {status.label}
                  </div>
                </div>

                <div className="my-order-items">
                  {order.items.map((item) => (
                    <div className="my-order-item" key={item.id ?? item.product.id}>
                      <div className="my-order-item-img">
                        {item.product.image ? (
                          <img src={item.product.image} alt={item.product.name} loading="lazy" />
                        ) : (
                          <div className="img-placeholder"><Package size={20} /></div>
                        )}
                      </div>
                      <div className="my-order-item-info">
                        <Link to={`/products/${item.product.id}`} className="my-order-item-name">{item.product.name}</Link>
                        {(item.size || item.color) && (
                          <p className="my-order-item-variant">
                            {item.color} {item.color && item.size ? "·" : ""} {item.size}
                          </p>
                        )}
                        <div className="my-order-item-price-row">
                          <span className="my-order-item-qty">x{item.quantity}</span>
                          <span className="my-order-item-price">{money(item.unitPrice ?? item.product.price)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="my-order-footer">
                  <div className="my-order-details">
                    <div className="detail-col">
                      <span className="detail-label">Giao đến</span>
                      <p>{order.address.name} - {order.address.phone}</p>
                      <p>{order.address.street}, {order.address.ward ? `${order.address.ward}, ` : ""}{order.address.district}, {order.address.city}</p>
                    </div>
                    <div className="detail-col">
                      <span className="detail-label">Thanh toán</span>
                      <p>{order.payment === "COD" ? "Thanh toán khi nhận hàng (COD)" : order.payment}</p>
                    </div>
                  </div>

                  <div className="my-order-summary">
                    {order.shipping != null && order.shipping > 0 && (
                      <div className="summary-line text-sm">
                        <span>Phí vận chuyển</span>
                        <span>{money(order.shipping)}</span>
                      </div>
                    )}
                    {order.discount != null && order.discount > 0 && (
                      <div className="summary-line text-sm discount">
                        <span>Giảm giá</span>
                        <span>-{money(order.discount)}</span>
                      </div>
                    )}
                    <div className="summary-line total">
                      <span>Tổng tiền</span>
                      <strong>{money(order.total)}</strong>
                    </div>

                    <div className="my-order-actions">
                      {order.status === "CHO_XAC_NHAN" && (
                        <Button 
                          variant="secondary" 
                          disabled={cancelling === order.id}
                          onClick={() => void handleCancel(order.id)}
                        >
                          {cancelling === order.id ? "Đang huỷ…" : "Huỷ đơn hàng"}
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
