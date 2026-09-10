import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Check, ArrowRight, LockKeyhole } from "lucide-react";
import type { Address, Order } from "../types";
import { useStore } from "../context/StoreContext";
import { isDemo } from "../data/config";
import { orderService } from "../services/orderService";
import { accountService } from "../services/accountService";
import { Input } from "../components/common/Input";
import { Button } from "../components/common/Button";
import { EmptyState, ErrorState, Loading } from "../components/common/Feedback";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { CartSummary } from "../components/cart/CartSummary";
import { money, errorMessage } from "../utils/format";
import { validateAddress, validEmail } from "../utils/validation";
export default function Checkout() {
  const { cart, clearCart, user, cartLoading, cartError, reloadCart, notify } =
    useStore();
  const [address, setAddress] = useState<Address>({
    name: user?.name ?? "",
    phone: user?.phone ?? "",
    street: "",
    city: "",
    district: "",
  });
  const [email, setEmail] = useState(user?.email ?? "");
  const [payment, setPayment] = useState("cod");
  const [note, setNote] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [addressLoading, setAddressLoading] = useState(false);
  const [order, setOrder] = useState<Order | null>(null);
  const requestId = useRef(crypto.randomUUID());
  const submitted = useRef(false);
  const formRef = useRef<HTMLFormElement>(null);
  useEffect(() => {
    if (!user) return;
    // Session restoration is asynchronous; preserve anything already typed.
    setAddress((current) => ({
      ...current,
      name: current.name || user.name,
      phone: current.phone || user.phone || "",
    }));
    setEmail((current) => current || user.email);
  }, [user]);
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (submitted.current || busy || cartLoading || cartError) return;
    const issues = validateAddress(address);
    if (!validEmail(email))
      issues.email = "Vui lòng nhập địa chỉ email hợp lệ.";
    setErrors(issues);
    setError("");
    if (Object.keys(issues).length) {
      setTimeout(
        () =>
          formRef.current
            ?.querySelector<HTMLInputElement>('[aria-invalid="true"]')
            ?.focus(),
        0,
      );
      return;
    }
    setBusy(true);
    submitted.current = true;
    try {
      const result = await orderService.create(
        { items: cart, address, email: email.trim(), payment, note },
        requestId.current,
      );
      setOrder(result);
      clearCart();
      window.scrollTo(0, 0);
    } catch (e) {
      setError(errorMessage(e));
      submitted.current = false;
    } finally {
      setBusy(false);
    }
  }
  if (order)
    return (
      <div className="container page">
        <div className="order-success">
          <div className="success-icon">
            <Check size={30} />
          </div>
          <div className="eyebrow">CẢM ƠN BẠN ĐÃ CHỌN NÉT</div>
          <h1>{isDemo ? "Đã tạo đơn hàng demo" : "Đã tiếp nhận đơn hàng"}</h1>
          <p>
            {isDemo
              ? "Đây là giao dịch minh họa. Không có thanh toán hoặc giao hàng thực tế."
              : "Bạn có thể theo dõi tiến trình trong tài khoản của mình."}
          </p>
          <div className="success-info">
            <div>
              <span>Mã đơn hàng</span>
              <strong>{order.id}</strong>
            </div>
            <div>
              <span>Tổng tiền</span>
              <strong>{money(order.total)}</strong>
            </div>
            <div>
              <span>Người nhận</span>
              <strong>{order.address.name}</strong>
            </div>
          </div>
          <Link to="/products" className="btn btn-primary">
            Tiếp tục khám phá <ArrowRight size={17} />
          </Link>
        </div>
      </div>
    );
  if (cartError)
    return (
      <div className="container page">
        <ErrorState message={cartError} retry={reloadCart} />
      </div>
    );
  if (cartLoading)
    return (
      <div className="container page">
        <Loading count={2} />
      </div>
    );
  if (!cart.length)
    return (
      <div className="container page">
        <EmptyState title="Chưa có sản phẩm để thanh toán">
          <Link className="btn btn-primary" to="/products">
            Khám phá sản phẩm
          </Link>
        </EmptyState>
      </div>
    );
  const update = (field: keyof Address, value: string) =>
    setAddress((a) => ({ ...a, [field]: value }));
  return (
    <div className="container page">
      <Breadcrumbs
        items={[{ label: "Giỏ hàng", to: "/cart" }, { label: "Thanh toán" }]}
      />
      <h1 className="page-title">Hoàn tất lựa chọn của bạn</h1>
      <p className="page-intro">
        Một bước nữa để những điều bạn yêu đến bên bạn.
      </p>
      {isDemo && (
        <div className="info-banner">
          Chế độ trải nghiệm: không thu tiền và không tạo đơn hàng thật.
        </div>
      )}
      <form
        ref={formRef}
        noValidate
        onSubmit={(event) => void submit(event)}
        className="checkout-layout"
      >
        <div>
          <section className="form-section">
            <h2>
              <span>01</span>Thông tin khách hàng
            </h2>
            <div className="form-grid">
              <Input
                label="Họ và tên *"
                autoComplete="name"
                value={address.name}
                error={errors.name}
                onChange={(e) => update("name", e.target.value)}
              />
              <Input
                label="Số điện thoại *"
                type="tel"
                autoComplete="tel"
                value={address.phone}
                error={errors.phone}
                onChange={(e) => update("phone", e.target.value)}
              />
            </div>
            <Input
              label="Email *"
              type="email"
              autoComplete="email"
              value={email}
              error={errors.email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </section>
          <section className="form-section">
            <h2>
              <span>02</span>Địa chỉ giao hàng
            </h2>
            {user && (
              <Button
                type="button"
                variant="secondary"
                className="mb-5"
                loading={addressLoading}
                onClick={async () => {
                  setAddressLoading(true);
                  try {
                    const saved = await accountService.getAddress(user.email);
                    if (saved) {
                      setAddress(saved);
                      setErrors({});
                      notify("Đã điền địa chỉ đã lưu");
                    } else notify("Bạn chưa lưu địa chỉ trong tài khoản.");
                  } catch (error) {
                    notify(errorMessage(error), true);
                  } finally {
                    setAddressLoading(false);
                  }
                }}
              >
                Dùng địa chỉ đã lưu
              </Button>
            )}
            <Input
              label="Số nhà, tên đường *"
              autoComplete="address-line1"
              value={address.street}
              error={errors.street}
              onChange={(e) => update("street", e.target.value)}
            />
            <div className="form-grid">
              <Input
                label="Tỉnh / Thành phố *"
                autoComplete="address-level1"
                value={address.city}
                error={errors.city}
                onChange={(e) => update("city", e.target.value)}
              />
              <Input
                label="Phường / Xã, Quận / Huyện *"
                autoComplete="address-level2"
                value={address.district}
                error={errors.district}
                onChange={(e) => update("district", e.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="note">Ghi chú (không bắt buộc)</label>
              <textarea
                id="note"
                rows={3}
                placeholder="Lời nhắn cho NÉT…"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                maxLength={500}
              />
            </div>
          </section>
          <fieldset className="form-section payment-section">
            <legend>
              <span>03</span>Phương thức thanh toán
            </legend>
            <label className="payment-option">
              <input
                type="radio"
                name="payment"
                value="cod"
                checked={payment === "cod"}
                onChange={(e) => setPayment(e.target.value)}
              />
              <span>
                Thanh toán khi nhận hàng (COD)
                <small>
                  Thanh toán cho đơn vị giao hàng khi nhận sản phẩm.
                </small>
              </span>
            </label>
            <label className="payment-option">
              <input
                type="radio"
                name="payment"
                value="bank_transfer"
                checked={payment === "bank_transfer"}
                onChange={(e) => setPayment(e.target.value)}
              />
              <span>
                Chuyển khoản ngân hàng
                <small>
                  {isDemo
                    ? "Tùy chọn minh họa, không thực hiện chuyển tiền."
                    : "Cửa hàng sẽ cung cấp hướng dẫn sau khi xác nhận đơn hàng."}
                </small>
              </span>
            </label>
          </fieldset>
        </div>
        <div>
          <div className="checkout-products">
            <h2>Sản phẩm của bạn</h2>
            {cart.map((item) => (
              <div className="mini-cart-item" key={item.product.id}>
                <img src={item.product.image} alt={item.product.name} />
                <div>
                  <h3>{item.product.name}</h3>
                  <p>
                    {item.quantity} × {money(item.product.price)}
                  </p>
                </div>
              </div>
            ))}
          </div>
          <CartSummary items={cart}>
            {error && (
              <p className="error-banner" role="alert">
                {error}
              </p>
            )}
            <Button className="w-full" type="submit" loading={busy}>
              {isDemo ? "Đặt hàng demo" : "Đặt hàng"}
              <ArrowRight size={17} />
            </Button>
            <p className="secure-note">
              <LockKeyhole size={14} />
              {isDemo
                ? "Không phát sinh giao dịch thật"
                : "Kiểm tra thông tin trước khi đặt hàng"}
            </p>
          </CartSummary>
        </div>
      </form>
    </div>
  );
}
