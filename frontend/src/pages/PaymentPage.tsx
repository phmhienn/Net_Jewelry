import { useCallback, useEffect, useRef, useState } from "react";
import { Link, Navigate, useLocation, useNavigate, useParams } from "react-router-dom";
import { CheckCircle2, ArrowRight } from "lucide-react";
import type { Order, PaymentStatus } from "../types";
import { useStore } from "../context/StoreContext";
import { paymentService } from "../services/paymentService";
import { orderService } from "../services/orderService";
import { SePayPayment } from "../components/payment/SePayPayment";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { ErrorState, Loading } from "../components/common/Feedback";
import { money, errorMessage } from "../utils/format";

export default function PaymentPage({ success = false }: { success?: boolean }) {
  const { orderId = "" } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { user, authLoading } = useStore();
  const initialOrder = (location.state as { order?: Order } | null)?.order ?? null;
  const [order, setOrder] = useState<Order | null>(initialOrder);
  const [status, setStatus] = useState<PaymentStatus | null>(null);
  const [loading, setLoading] = useState(!initialOrder && !!orderId && !success);
  const [checking, setChecking] = useState(false);
  const [error, setError] = useState("");
  const mounted = useRef(true);

  const checkStatus = useCallback(async () => {
    if (!orderId) return;
    setChecking(true);
    setError("");
    try {
      const next = await paymentService.status(orderId);
      if (!mounted.current) return;
      setStatus(next);
      if (next.paymentStatus === "CONFIRMED") {
        navigate("/payment/success", { replace: true, state: { status: next, order } });
      } else if (next.paymentStatus === "FAILED") {
        setError("Thanh toán không thành công. Vui lòng liên hệ cửa hàng để được hỗ trợ.");
      }
    } catch (err) {
      if (mounted.current) setError(errorMessage(err));
    } finally {
      if (mounted.current) setChecking(false);
    }
  }, [navigate, order, orderId]);

  useEffect(() => {
    mounted.current = true;
    return () => { mounted.current = false; };
  }, []);

  useEffect(() => {
    if (success || order || !orderId) return;
    orderService.get(orderId).then(setOrder).catch((err) => setError(errorMessage(err))).finally(() => setLoading(false));
  }, [order, orderId, success]);

  useEffect(() => {
    if (success || !orderId) return undefined;
    void checkStatus();
    const timer = window.setInterval(() => void checkStatus(), 4000);
    return () => window.clearInterval(timer);
  }, [checkStatus, orderId, success]);

  if (authLoading) return <div className="container page"><Loading count={1} /></div>;
  if (!user) return <Navigate to="/login" replace state={{ from: `${location.pathname}${location.search}` }} />;

  if (success) {
    const paid = (location.state as { status?: PaymentStatus; order?: Order } | null)?.status;
    return <div className="container page"><div className="order-success payment-success">
      <div className="success-icon"><CheckCircle2 size={30} /></div>
      <div className="eyebrow">ĐÃ NHẬN THANH TOÁN</div>
      <h1>Thanh toán thành công</h1>
      <p>Đơn hàng của bạn đã được SePay xác nhận. Cửa hàng sẽ tiếp tục xử lý đơn.</p>
      <div className="success-info">
        <div><span>Mã đơn hàng</span><strong>{paid?.orderCode ?? "—"}</strong></div>
        <div><span>Số tiền</span><strong>{paid ? money(paid.amount) : "—"}</strong></div>
        <div><span>Trạng thái</span><strong>Đã thanh toán</strong></div>
      </div>
      <Link to="/account?tab=orders" className="btn btn-primary">Xem đơn hàng <ArrowRight size={17} /></Link>
    </div></div>;
  }

  if (loading) return <div className="container page"><Loading count={2} /></div>;
  if (!order) return <div className="container page"><ErrorState message={error || "Không tìm thấy thông tin thanh toán."} retry={() => window.location.reload()} /></div>;

  return <div className="container page payment-page">
    <Breadcrumbs items={[{ label: "Đơn hàng", to: "/account?tab=orders" }, { label: "Thanh toán" }]} />
    {error && <p className="error-banner" role="alert">{error}</p>}
    <SePayPayment order={order} checking={checking} onCheck={() => void checkStatus()} />
    {status && <p className="payment-poll-note">Trạng thái hiện tại: <strong>{status.paymentStatus === "CONFIRMED" ? "Đã thanh toán" : status.paymentStatus === "FAILED" ? "Thất bại" : "Đang chờ thanh toán"}</strong></p>}
  </div>;
}
