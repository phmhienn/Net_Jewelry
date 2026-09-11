import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Check, ArrowRight, LockKeyhole, QrCode, Truck } from "lucide-react";
import type { Address, Order } from "../types";
import { useStore } from "../context/StoreContext";
import { orderService } from "../services/orderService";
import { accountService } from "../services/accountService";
import { Input } from "../components/common/Input";
import { Button } from "../components/common/Button";
import { EmptyState, ErrorState, Loading } from "../components/common/Feedback";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { CartSummary } from "../components/cart/CartSummary";
import { money, errorMessage } from "../utils/format";
import { validateAddress } from "../utils/validation";

export default function Checkout() {
  const { cart, clearCart, user, cartLoading, cartError, reloadCart, notify } = useStore();
  const navigate = useNavigate();
  const [address, setAddress] = useState<Address>({ name: user?.name ?? "", phone: user?.phone ?? "", street: "", city: "", district: "", ward: "" });
  const [note, setNote] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [addressLoading, setAddressLoading] = useState(false);
  const [payment, setPayment] = useState<"COD" | "BANK_TRANSFER">("COD");
  const [order, setOrder] = useState<Order | null>(null);
  const requestId = useRef(crypto.randomUUID());
  const submitted = useRef(false);

  useEffect(() => {
    if (!user) return;
    setAddress((current) => ({ ...current, name: current.name || user.name, phone: current.phone || user.phone || "" }));
  }, [user]);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (submitted.current || busy || cartLoading || cartError) return;
    const issues = validateAddress(address);
    setErrors(issues);
    setError("");
    if (Object.keys(issues).length) return;
    setBusy(true);
    submitted.current = true;
    try {
      const result = await orderService.create({ address, payment, note }, requestId.current.replaceAll("-", ""));
      await clearCart();
      if (payment === "BANK_TRANSFER") {
        navigate(`/payment/${result.databaseId ?? result.paymentDetail?.orderId ?? result.id}`, { replace: true, state: { order: result } });
        return;
      }
      setOrder(result);
      window.scrollTo(0, 0);
    } catch (e) {
      setError(errorMessage(e));
      submitted.current = false;
    } finally {
      setBusy(false);
    }
  }

  if (order) return (
    <div className="container page"><div className="order-success">
      <div className="success-icon"><Check size={30} /></div>
      <div className="eyebrow">CẢM ƠN BẠN ĐÃ CHỌN NÉT</div>
      <h1>Đã tiếp nhận đơn hàng</h1>
      <p>Bạn có thể theo dõi tiến trình trong tài khoản của mình.</p>
      <div className="success-info">
        <div><span>Mã đơn hàng</span><strong>{order.id}</strong></div>
        <div><span>Tổng tiền</span><strong>{money(order.total)}</strong></div>
        <div><span>Người nhận</span><strong>{order.address.name}</strong></div>
      </div>
      <Link to="/products" className="btn btn-primary">Tiếp tục khám phá <ArrowRight size={17} /></Link>
    </div></div>
  );
  if (cartError) return <div className="container page"><ErrorState message={cartError} retry={reloadCart} /></div>;
  if (cartLoading) return <div className="container page"><Loading count={2} /></div>;
  if (!cart.length) return <div className="container page"><EmptyState title="Chưa có sản phẩm để thanh toán"><Link className="btn btn-primary" to="/products">Khám phá sản phẩm</Link></EmptyState></div>;

  const update = (field: keyof Address, value: string) => setAddress((a) => ({ ...a, [field]: value }));
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Giỏ hàng", to: "/cart" }, { label: "Thanh toán" }]} />
      <h1 className="page-title">Hoàn tất đơn hàng</h1>
      <p className="page-intro">Chọn COD hoặc chuyển khoản ngân hàng qua VietQR/SePay. Website chỉ xác nhận chuyển khoản khi webhook SePay báo giao dịch hợp lệ.</p>
      <form noValidate onSubmit={(event) => void submit(event)} className="checkout-layout">
        <div>
          <section className="form-section">
            <h2><span>01</span>Thông tin khách hàng</h2>
            <div className="form-grid">
              <Input label="Họ và tên *" autoComplete="name" value={address.name} error={errors.name} onChange={(e) => update("name", e.target.value)} />
              <Input label="Số điện thoại *" type="tel" autoComplete="tel" value={address.phone} error={errors.phone} onChange={(e) => update("phone", e.target.value)} />
            </div>
          </section>
          <section className="form-section">
            <h2><span>02</span>Địa chỉ giao hàng</h2>
            {user && <Button type="button" variant="secondary" className="mb-5" loading={addressLoading} onClick={async () => {
              setAddressLoading(true);
              try {
                const saved = await accountService.getAddress();
                if (saved) { setAddress(saved); setErrors({}); notify("Đã điền địa chỉ đã lưu"); }
                else notify("Bạn chưa lưu địa chỉ trong tài khoản.");
              } catch (err) { notify(errorMessage(err), true); }
              finally { setAddressLoading(false); }
            }}>Dùng địa chỉ đã lưu</Button>}
            <Input label="Số nhà, tên đường *" autoComplete="address-line1" value={address.street} error={errors.street} onChange={(e) => update("street", e.target.value)} />
            <div className="form-grid">
              <Input label="Tỉnh / Thành phố *" autoComplete="address-level1" value={address.city} error={errors.city} onChange={(e) => update("city", e.target.value)} />
              <Input label="Quận / Huyện *" autoComplete="address-level2" value={address.district} error={errors.district} onChange={(e) => update("district", e.target.value)} />
            </div>
            <Input label="Phường / Xã" value={address.ward ?? ""} onChange={(e) => update("ward", e.target.value)} />
            <div className="field"><label htmlFor="note">Ghi chú</label><textarea id="note" rows={3} value={note} onChange={(e) => setNote(e.target.value)} maxLength={500} /></div>
          </section>
          <fieldset className="form-section payment-section">
            <legend><span>03</span>Phương thức thanh toán</legend>
            <label className="payment-option">
              <input type="radio" name="payment" checked={payment === "COD"} onChange={() => setPayment("COD")} />
              <span className="payment-option-icon" aria-hidden="true"><Truck size={20} /></span>
              <span>
                Thanh toán khi nhận hàng (COD)
                <small>Thanh toán cho đơn vị giao hàng khi nhận sản phẩm.</small>
              </span>
            </label>
            <label className="payment-option">
              <input type="radio" name="payment" checked={payment === "BANK_TRANSFER"} onChange={() => setPayment("BANK_TRANSFER")} />
              <span className="payment-option-icon" aria-hidden="true"><QrCode size={20} /></span>
              <span>
                Chuyển khoản QR VietQR / SePay
                <small>Nhận mã QR sau khi đặt hàng, chuyển đúng số tiền và nội dung để SePay tự xác nhận thanh toán.</small>
              </span>
            </label>
          </fieldset>
        </div>
        <div>
          <div className="checkout-products"><h2>Sản phẩm của bạn</h2>{cart.map((item) => <div className="mini-cart-item" key={item.id ?? item.product.id}>{item.product.image ? <img src={item.product.image} alt={item.product.name} /> : <span className="mini-image-placeholder" />}<div><h3>{item.product.name}</h3><p>{item.quantity} × {money(item.unitPrice ?? item.product.price)}</p></div></div>)}</div>
          <CartSummary items={cart}>{error && <p className="error-banner" role="alert">{error}</p>}<Button className="w-full" type="submit" loading={busy}>{payment === "BANK_TRANSFER" ? "Tạo đơn và mở mã QR" : "Đặt hàng"} <ArrowRight size={17} /></Button><p className="secure-note"><LockKeyhole size={14} />Kiểm tra thông tin trước khi đặt hàng</p></CartSummary>
        </div>
      </form>
    </div>
  );
}
