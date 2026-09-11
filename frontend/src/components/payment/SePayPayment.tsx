import { CheckCircle2 } from "lucide-react";
import type { Order } from "../../types";
import { money } from "../../utils/format";

export function SePayPayment({ order, timeLeft, expired }: { order: Order; timeLeft?: string; expired?: boolean }) {
  const instruction = order.paymentDetail?.instruction;
  return (
    <section className="sepay-card">
      <div className="sepay-heading">
        <div>
          <p className="eyebrow">THANH TOÁN CHUYỂN KHOẢN</p>
          <h1>Thanh toán đơn hàng</h1>
          <p>Quét mã QR hoặc chuyển khoản đúng nội dung bên dưới để hệ thống tự xác nhận qua SePay.</p>
        </div>
        <span className={`payment-status-pill ${expired ? "failed" : "pending"}`}>{expired ? "Đã hết hạn" : "Đang chờ thanh toán"}</span>
      </div>
      <div className={`sepay-expiry ${expired ? "expired" : ""}`}>
        {expired ? "Mã QR đã hết hạn. Đơn hàng đã được tự động hủy." : <>Mã QR còn hiệu lực: <strong>{timeLeft ?? "30:00"}</strong></>}
      </div>

      <div className="sepay-layout">
        <div className="sepay-qr-box">
          {instruction?.qrUrl ? <img src={instruction.qrUrl} alt={`QR thanh toán đơn ${order.code ?? order.id}`} /> : <div className="sepay-qr-empty">Chưa cấu hình VietQR</div>}
        </div>
        <dl className="sepay-details">
          <div><dt>Mã đơn hàng</dt><dd>{order.code ?? order.id}</dd></div>
          <div><dt>Số tiền</dt><dd>{money(instruction?.amount ?? order.total)}</dd></div>
          <div><dt>Ngân hàng</dt><dd>{instruction?.bankCode || "Chưa cấu hình"}</dd></div>
          <div><dt>Số tài khoản</dt><dd>{instruction?.accountNumber || "Chưa cấu hình"}</dd></div>
          <div><dt>Chủ tài khoản</dt><dd>{instruction?.accountName || "Chưa cấu hình"}</dd></div>
          <div className="sepay-content"><dt>Nội dung chuyển khoản</dt><dd>{instruction?.content ?? order.code ?? order.id}</dd></div>
        </dl>
      </div>

      <div className="sepay-actions">
        <p><CheckCircle2 size={15} />Hệ thống tự kiểm tra trạng thái thanh toán qua backend và webhook SePay.</p>
      </div>
    </section>
  );
}
