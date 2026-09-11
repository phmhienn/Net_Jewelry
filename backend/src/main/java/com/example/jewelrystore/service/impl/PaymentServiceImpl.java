package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.*;
import com.example.jewelrystore.util.Pages;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
  private final ThanhToanRepository payments;
  private final DonHangRepository orders;
  private final GiaoDichRepository transactions;
  private final ChiTietDonHangRepository orderItems;
  private final InventoryService inventory;
  private final CouponService coupons;
  private final DeliveryService deliveries;
  private final PaymentMapper paymentMapper;
  private final CurrentActor actor;

  @Value("${payment.bank-transfer.expire-minutes:30}")
  private long bankTransferExpireMinutes;

  private ThanhToan locked(Long id) {
    // Resolve a scalar ID first: do not cache a stale payment before acquiring the order lock.
    Long orderId =
        payments
            .orderId(id)
            .orElseThrow(
                () ->
                    new com.example.jewelrystore.exception.ResourceNotFoundException(
                        "Không tìm thấy thanh toán"));
    lock(orders, orderId);
    var payment = lock(payments, id);
    if (actor.get().customer()) owner(payment.getOrder().getCustomer().getId(), actor.customerId());
    require(payment.getOrder().getStatus() != OrderStatus.DA_HUY, "Đơn hàng đã hủy");
    return payment;
  }

  @Transactional(readOnly = true)
  public PaymentResponse get(Long id) {
    ThanhToan tt = com.example.jewelrystore.util.Checks.get(payments, id);
    if (actor.get().customer()) owner(tt.getOrder().getCustomer().getId(), actor.customerId());
    return paymentMapper.payment(tt);
  }

  public PaymentResponse confirm(Long id) {
    ThanhToan tt = locked(id);
    require(
        tt.getMethod() != PaymentMethod.COD || tt.getOrder().getStatus() == OrderStatus.HOAN_THANH,
        "COD được xác nhận khi hoàn tất đơn");
    record(tt, true);
    return paymentMapper.payment(tt);
  }

  public void completeCod(DonHang order) {
    ThanhToan tt = payments.findByOrderId(order.getId()).orElseThrow();
    if (tt.getMethod() == PaymentMethod.COD) record(tt, true);
    else require(tt.getStatus() == PaymentStatus.CONFIRMED, "Đơn hàng chưa thanh toán");
  }

  private void record(ThanhToan tt, boolean success) {
    if (tt.getStatus() == PaymentStatus.CONFIRMED) return;
    tt.setStatus(success ? PaymentStatus.CONFIRMED : PaymentStatus.FAILED);
    tt.setPaidAt(success ? Instant.now() : null);
    GiaoDich gd = new GiaoDich();
    gd.setPayment(tt);
    gd.setCode("GD-" + UUID.randomUUID());
    gd.setAmount(tt.getAmount());
    gd.setMethod(tt.getMethod());
    gd.setStatus(success ? TransactionStatus.THANH_CONG : TransactionStatus.THAT_BAI);
    transactions.save(gd);
  }

  @Transactional(readOnly = true)
  public PageResponse<PaymentResponse> list(int page, int size) {
    return PageResponse.of(payments.findAll(Pages.of(page, size)).map(paymentMapper::payment));
  }

  public PaymentStatusResponse statusByOrder(Long orderId) {
    DonHang dh = lock(orders, orderId);
    if (actor.get().customer()) owner(dh.getCustomer().getId(), actor.customerId());
    ThanhToan tt = payments.findByOrderId(orderId).orElseThrow();
    expireBankTransferIfNeeded(dh, tt);
    return new PaymentStatusResponse(
        dh.getId(),
        dh.getCode(),
        tt.getStatus(),
        tt.getMethod(),
        tt.getAmount(),
        expiresAt(dh, tt),
        tt.getStatus() == PaymentStatus.FAILED && dh.getStatus() == OrderStatus.DA_HUY);
  }

  public void expireBankTransferIfNeeded(DonHang order, ThanhToan payment) {
    if (bankTransferExpireMinutes <= 0) return;
    if (payment.getMethod() != PaymentMethod.BANK_TRANSFER) return;
    if (payment.getStatus() != PaymentStatus.PENDING) return;
    if (order.getStatus() != OrderStatus.CHO_XAC_NHAN) return;
    Instant expiresAt = expiresAt(order, payment);
    if (expiresAt == null || Instant.now().isBefore(expiresAt)) return;

    for (var line : orderItems.findByOrderIdOrderByVariantId(order.getId())) {
      inventory.apply(
          inventory.lockStock(line.getVariant().getId()),
          0,
          -line.getQuantity(),
          StockAction.GIAI_PHONG,
          "Hết hạn thanh toán QR đơn " + order.getId(),
          order.getCustomer().getId());
    }
    payment.setStatus(PaymentStatus.FAILED);
    payment.setPaidAt(null);
    order.setStatus(OrderStatus.DA_HUY);
    order.setCancelledAt(Instant.now());
    order.setCancelReason("Tự động hủy do quá hạn thanh toán QR");
    coupons.release(order);
    deliveries.cancel(order);
  }

  private Instant expiresAt(DonHang order, ThanhToan payment) {
    if (bankTransferExpireMinutes <= 0 || payment.getMethod() != PaymentMethod.BANK_TRANSFER) {
      return null;
    }
    return order.getDate().plusSeconds(bankTransferExpireMinutes * 60);
  }

  @Transactional(readOnly = true)
  public PageResponse<TransactionResponse> transactions(Long paymentId, int page, int size) {
    return PageResponse.of(
        (paymentId == null
                ? transactions.findAll(Pages.of(page, size))
                : transactions.findByPaymentId(paymentId, Pages.of(page, size)))
            .map(paymentMapper::transaction));
  }
}
