package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.config.SePayConfig;
import com.example.jewelrystore.dto.request.SePayWebhookRequest;
import com.example.jewelrystore.entity.GiaoDich;
import com.example.jewelrystore.entity.ThanhToan;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.exception.BadRequestException;
import com.example.jewelrystore.repository.DonHangRepository;
import com.example.jewelrystore.repository.GiaoDichRepository;
import com.example.jewelrystore.repository.ThanhToanRepository;
import com.example.jewelrystore.service.PaymentService;
import com.example.jewelrystore.service.SePayService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SePayServiceImpl implements SePayService {
  private static final Logger log = LoggerFactory.getLogger(SePayServiceImpl.class);
  private static final Pattern PAYMENT_CODE = Pattern.compile("ORD[0-9A-Z]{8,40}");
  private static final DateTimeFormatter SEPAY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final SePayConfig config;
  private final DonHangRepository orders;
  private final ThanhToanRepository payments;
  private final GiaoDichRepository transactions;
  private final PaymentService paymentService;

  public void processWebhook(SePayWebhookRequest request) {
    String transactionCode = transactionCode(request);
    log.info("SePay webhook received transaction={}, reference={}, amount={}, transferType={}",
        request.id(), request.referenceCode(), request.transferAmount(), request.transferType());

    if (transactions.existsByCode(transactionCode)) {
      log.info("SePay duplicate transaction ignored transaction={}", transactionCode);
      return;
    }
    if (!"in".equalsIgnoreCase(String.valueOf(request.transferType()).trim())) {
      log.info("SePay outbound transaction ignored transaction={}", transactionCode);
      return;
    }
    if (request.transferAmount() == null || request.transferAmount() <= 0) {
      throw new BadRequestException("Số tiền giao dịch SePay không hợp lệ");
    }
    if (config.bankConfigured()
        && request.accountNumber() != null
        && !config.accountNumber().equals(request.accountNumber().trim())) {
      throw new BadRequestException("Tài khoản nhận tiền không hợp lệ");
    }

    String orderCode = extractPaymentCode(request);
    var order = orders.findByCode(orderCode).orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng theo mã thanh toán"));
    var payment = payments.findByOrderId(order.getId()).orElseThrow(() -> new BadRequestException("Không tìm thấy thanh toán của đơn hàng"));

    paymentService.expireBankTransferIfNeeded(order, payment);
    require(order.getStatus() != OrderStatus.DA_HUY, "Đơn hàng đã hủy");
    require(payment.getMethod() == PaymentMethod.BANK_TRANSFER, "Đơn hàng không dùng chuyển khoản ngân hàng");
    BigDecimal transferAmount = BigDecimal.valueOf(request.transferAmount());
    require(payment.getAmount().compareTo(transferAmount) == 0, "Số tiền thanh toán không khớp");
    if (payment.getStatus() == PaymentStatus.CONFIRMED) {
      log.info("SePay transaction ignored because payment already confirmed orderCode={}, transaction={}", orderCode, transactionCode);
      return;
    }

    saveTransaction(payment, request, transactionCode, TransactionStatus.THANH_CONG, "SePay xác nhận thanh toán");
    payment.setStatus(PaymentStatus.CONFIRMED);
    payment.setPaidAt(parseTime(request.transactionDate()));
    if (order.getStatus() == OrderStatus.CHO_XAC_NHAN) {
      order.setStatus(OrderStatus.DA_XAC_NHAN);
      order.setConfirmedAt(Instant.now());
    }
    log.info("SePay payment confirmed orderCode={}, paymentId={}, amount={}", orderCode, payment.getId(), transferAmount);
  }

  private String extractPaymentCode(SePayWebhookRequest request) {
    String raw = String.join(" ",
        String.valueOf(request.code()),
        String.valueOf(request.content()),
        String.valueOf(request.description()));
    var matcher = PAYMENT_CODE.matcher(raw.toUpperCase(Locale.ROOT));
    if (!matcher.find()) throw new BadRequestException("Không tìm thấy mã thanh toán trong nội dung chuyển khoản");
    return matcher.group();
  }

  private String transactionCode(SePayWebhookRequest request) {
    if (request.id() != null) return "SEPAY-" + request.id();
    if (request.referenceCode() != null && !request.referenceCode().isBlank()) return "SEPAY-" + request.referenceCode().trim();
    throw new BadRequestException("Thiếu mã giao dịch SePay");
  }

  private void saveTransaction(
      ThanhToan payment,
      SePayWebhookRequest request,
      String transactionCode,
      TransactionStatus status,
      String note) {
    if (transactions.existsByCode(transactionCode)) return;
    GiaoDich gd = new GiaoDich();
    gd.setPayment(payment);
    gd.setCode(transactionCode);
    gd.setAmount(BigDecimal.valueOf(request.transferAmount()));
    gd.setMethod(PaymentMethod.BANK_TRANSFER);
    gd.setStatus(status);
    gd.setTime(parseTime(request.transactionDate()));
    gd.setNote(note + " - ref: " + safe(request.referenceCode()) + ", gateway: " + safe(request.gateway()));
    transactions.save(gd);
  }

  private Instant parseTime(String value) {
    if (value == null || value.isBlank()) return Instant.now();
    try {
      return LocalDateTime.parse(value.trim(), SEPAY_DATE).atZone(ZoneId.systemDefault()).toInstant();
    } catch (Exception ignored) {
      return Instant.now();
    }
  }

  private String safe(String value) {
    return value == null || value.isBlank() ? "-" : value.trim();
  }
}
