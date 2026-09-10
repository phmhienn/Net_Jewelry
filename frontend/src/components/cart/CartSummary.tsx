import type { ReactNode } from "react";
import type { CartLine } from "../../types";
import { money, subtotal, shipping } from "../../utils/format";
export function CartSummary({
  items,
  children,
}: {
  items: CartLine[];
  children?: ReactNode;
}) {
  const amount = subtotal(items);
  const fee = shipping(amount);
  return (
    <aside className="cart-summary">
      <h2>Tóm tắt đơn hàng</h2>
      <div className="summary-row">
        <span>Tạm tính</span>
        <span>{money(amount)}</span>
      </div>
      <div className="summary-row">
        <span>Phí vận chuyển</span>
        <span>{fee ? money(fee) : "Miễn phí"}</span>
      </div>
      <div className="summary-total">
        <span>Tổng cộng</span>
        <strong>{money(amount + fee)}</strong>
      </div>
      {amount > 0 && amount < 1500000 && (
        <p className="shipping-note">
          Mua thêm {money(1500000 - amount)} để được miễn phí vận chuyển.
        </p>
      )}
      {children}
      <p className="summary-note">Giá hiển thị đã bao gồm thuế.</p>
    </aside>
  );
}
