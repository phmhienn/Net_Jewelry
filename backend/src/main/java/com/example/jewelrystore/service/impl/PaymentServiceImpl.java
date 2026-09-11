package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.PaymentService;
import com.example.jewelrystore.util.Pages;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
  private final ThanhToanRepository payments;
  private final DonHangRepository orders;
  private final GiaoDichRepository transactions;
  private final PaymentMapper paymentMapper;
  private final CurrentActor actor;

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

  @Transactional(readOnly = true)
  public PaymentStatusResponse statusByOrder(Long orderId) {
    DonHang dh = com.example.jewelrystore.util.Checks.get(orders, orderId);
    if (actor.get().customer()) owner(dh.getCustomer().getId(), actor.customerId());
    ThanhToan tt = payments.findByOrderId(orderId).orElseThrow();
    return new PaymentStatusResponse(dh.getId(), dh.getCode(), tt.getStatus(), tt.getMethod(), tt.getAmount());
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
