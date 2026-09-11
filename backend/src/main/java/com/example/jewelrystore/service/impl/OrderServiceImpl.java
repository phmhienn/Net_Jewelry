package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.*;
import com.example.jewelrystore.util.Pages;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
  private final TaiKhoanRepository customers;
  private final GioHangRepository carts;
  private final ChiTietGioHangRepository cartItems;
  private final BienTheSanPhamRepository variants;
  private final DonHangRepository orders;
  private final ChiTietDonHangRepository items;
  private final ThanhToanRepository payments;
  private final InventoryService inventory;
  private final PaymentService paymentService;
  private final OrderMapper orderMapper;
  private final CurrentActor actor;
  private final ObjectMapper json;

  private final ShippingService shipping;
  private final CouponService coupons;
  private final DeliveryService deliveries;

  public OrderResponse create(CreateOrderRequest request, String key) {
    require(
        key != null && key.matches("[A-Za-z0-9_-]{8,100}"),
        "Idempotency-Key phải có 8–100 ký tự chữ, số, gạch ngang");
    require(
        request.payment() == PaymentMethod.COD || request.payment() == PaymentMethod.BANK_TRANSFER,
        "Hiện chỉ hỗ trợ COD hoặc chuyển khoản ngân hàng");
    var customer = lock(customers, actor.customerId());
    String requestHash = hash(json.writeValueAsString(request));
    var previous = orders.findByCustomerIdAndIdempotencyKey(customer.getId(), key);
    if (previous.isPresent()) {
      require(
          previous.get().getRequestHash().equals(requestHash),
          "Idempotency-Key đã được dùng cho yêu cầu khác");
      return orderMapper.order(previous.get());
    }
    var cart =
        carts
            .findByCustomerId(customer.getId())
            .orElseThrow(() -> new BadRequestException("Giỏ hàng trống"));
    var lines =
        cartItems.findByCartIdOrderByVariantId(cart.getId()).stream()
            .filter(ChiTietGioHang::isSelected)
            .toList();
    require(!lines.isEmpty(), "Vui lòng chọn sản phẩm trong giỏ hàng");
    var stockById = new LinkedHashMap<Long, TonKho>();
    var variantById = new HashMap<Long, BienTheSanPham>();
    BigDecimal total = BigDecimal.ZERO;
    // Same ascending variant order for every checkout prevents lock-order deadlocks.
    for (var line : lines) {
      BienTheSanPham btsp = lock(variants, line.getVariant().getId());
      sellable(btsp);
      var stock = inventory.lockStock(btsp.getId());
      if (stock.getQuantity() - stock.getReserved() < line.getQuantity())
        throw new InsufficientStockException("Không đủ tồn kho cho " + btsp.getSku());
      stockById.put(btsp.getId(), stock);
      variantById.put(btsp.getId(), btsp);
      total = total.add(btsp.getEffectivePrice().multiply(BigDecimal.valueOf(line.getQuantity())));
    }
    var order = new DonHang();
    order.setCustomer(customer);
    order.setCode(nextOrderCode());
    order.setSubtotal(total);
    order.setShipping(shipping.fee(total));
    coupons.apply(order, request.couponCode());
    order.setTotal(total.subtract(order.getDiscount()).add(order.getShipping()));
    require(order.getTotal().signum() > 0, "Tổng thanh toán phải lớn hơn 0");
    order.setNote(request.note());
    var addressRequest = request.address();
    order.setRecipientName(addressRequest.name());
    order.setRecipientPhone(addressRequest.phone());
    order.setAddressSnapshot(
        json.writeValueAsString(
            new AddressResponse(
                null,
                addressRequest.name(),
                addressRequest.phone(),
                addressRequest.city(),
                addressRequest.district(),
                addressRequest.ward(),
                addressRequest.street(),
                false)));
    order.setShippingMethod("STANDARD");
    order.setIdempotencyKey(key);
    order.setRequestHash(requestHash);
    orders.save(order);
    coupons.record(order);
    deliveries.create(order);
    for (var line : lines) {
      BienTheSanPham btsp = variantById.get(line.getVariant().getId());
      var item = new ChiTietDonHang();
      item.setOrder(order);
      item.setVariant(btsp);
      item.setProductName(btsp.getProduct().getName());
      item.setSku(btsp.getSku());
      item.setSize(btsp.getSize());
      item.setColor(btsp.getColor());
      item.setQuantity(line.getQuantity());
      item.setUnitPrice(btsp.getEffectivePrice());
      item.setTotal(btsp.getEffectivePrice().multiply(BigDecimal.valueOf(line.getQuantity())));
      items.save(item);
      inventory.apply(
          stockById.get(btsp.getId()),
          0,
          line.getQuantity(),
          StockAction.GIU_HANG,
          "Giữ hàng cho đơn " + order.getId(),
          customer.getId());
    }
    var payment = new ThanhToan();
    payment.setOrder(order);
    payment.setAmount(order.getTotal());
    payment.setMethod(request.payment());
    payments.save(payment);
    cartItems.deleteAll(lines);
    cartItems.flush();
    cart.setTotal(
        cartItems.findByCartIdOrderByVariantId(cart.getId()).stream()
            .map(ChiTietGioHang::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    cart.setUpdatedAt(Instant.now());
    return orderMapper.order(order);
  }

  private String nextOrderCode() {
    String prefix = "ORD" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    String code;
    do {
      code = prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    } while (orders.existsByCode(code));
    return code;
  }

  @Transactional(readOnly = true)
  public PageResponse<OrderResponse> list(int page, int size) {
    return PageResponse.of(
        orders.findByCustomerId(actor.customerId(), Pages.of(page, size)).map(orderMapper::order));
  }

  @Transactional(readOnly = true)
  public PageResponse<OrderResponse> adminList(
      String status, Long customerId, String keyword, String date, int page, int size) {
    OrderStatus filter = null;
    if (status != null && !status.isBlank()) {
      try {
        filter = OrderStatus.valueOf(status.trim());
      } catch (Exception e) {
        throw new BadRequestException("Trạng thái không hợp lệ");
      }
    }
    final var state = filter;
    final var search =
        keyword == null || keyword.isBlank() ? null : "%" + keyword.trim().toLowerCase() + "%";
    final Instant from;
    final Instant to;
    if (date == null || date.isBlank()) {
      from = null;
      to = null;
    } else {
      try {
        var selectedDate = LocalDate.parse(date);
        from = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        to = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
      } catch (Exception e) {
        throw new BadRequestException("Ngày đặt không hợp lệ");
      }
    }
    return PageResponse.of(
        orders
            .findAll(
                (root, criteriaQuery, cb) -> {
                  var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
                  if (state != null) predicates.add(cb.equal(root.get("status"), state));
                  if (customerId != null)
                    predicates.add(cb.equal(root.get("customer").get("id"), customerId));
                  if (search != null) predicates.add(cb.like(cb.lower(root.get("code")), search));
                  if (from != null && to != null)
                    predicates.add(cb.between(root.get("date"), from, to));
                  return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                },
                Pages.of(page, size))
            .map(orderMapper::order));
  }

  @Transactional(readOnly = true)
  public OrderResponse get(Long id) {
    DonHang dh = com.example.jewelrystore.util.Checks.get(orders, id);
    owned(dh);
    return orderMapper.order(dh);
  }

  private void owned(DonHang dh) {
    if (actor.get().customer()) owner(dh.getCustomer().getId(), actor.customerId());
  }

  public OrderResponse cancel(Long id) {
    DonHang dh = lock(orders, id);
    owned(dh);
    cancelOrder(dh);
    return orderMapper.order(dh);
  }

  private void cancelOrder(DonHang dh) {
    if (dh.getStatus() == OrderStatus.DA_HUY) return;
    if (dh.getStatus() == OrderStatus.DANG_GIAO_HANG || dh.getStatus() == OrderStatus.HOAN_THANH)
      throw new InvalidOrderStatusException("Không thể hủy đơn đã giao vận");
    require(
        payments.findByOrderId(dh.getId()).orElseThrow().getStatus() != PaymentStatus.CONFIRMED,
        "Đơn đã thanh toán cần liên hệ cửa hàng để được hỗ trợ");
    for (var line : items.findByOrderIdOrderByVariantId(dh.getId()))
      inventory.apply(
          inventory.lockStock(line.getVariant().getId()),
          0,
          -line.getQuantity(),
          StockAction.GIAI_PHONG,
          "Hủy đơn " + dh.getId(),
          actor.get().id());
    if (!actor.get().customer())
      dh.setStaff(com.example.jewelrystore.util.Checks.get(customers, actor.get().id()));
    dh.setStatus(OrderStatus.DA_HUY);
    coupons.release(dh);
    deliveries.cancel(dh);
    dh.setCancelledAt(Instant.now());
    dh.setCancelReason("Hủy theo yêu cầu " + (actor.get().customer() ? "khách hàng" : "nhân viên"));
  }

  public OrderResponse status(Long id, OrderStatusRequest request) {
    DonHang dh = lock(orders, id);
    if (dh.getStatus() == request.status()) return orderMapper.order(dh);
    if (request.status() == OrderStatus.DA_HUY) {
      cancelOrder(dh);
      return orderMapper.order(dh);
    }
    OrderStatus next =
        switch (dh.getStatus()) {
          case CHO_XAC_NHAN -> OrderStatus.DA_XAC_NHAN;
          case DA_XAC_NHAN -> OrderStatus.DANG_XU_LY;
          case DANG_XU_LY -> OrderStatus.DANG_GIAO_HANG;
          case DANG_GIAO_HANG -> OrderStatus.HOAN_THANH;
          default -> null;
        };
    if (next == null || next != request.status())
      throw new InvalidOrderStatusException(
          "Không thể chuyển từ " + dh.getStatus() + " sang " + request.status());
    if (next == OrderStatus.DANG_GIAO_HANG) {
      var payment = payments.findByOrderId(id).orElseThrow();
      require(
          payment.getMethod() == PaymentMethod.COD
              || payment.getStatus() == PaymentStatus.CONFIRMED,
          "Phải thanh toán trước khi giao hàng");
      for (var line : items.findByOrderIdOrderByVariantId(id))
        inventory.apply(
            inventory.lockStock(line.getVariant().getId()),
            -line.getQuantity(),
            -line.getQuantity(),
            StockAction.GIAO_HANG,
            "Giao đơn " + id,
            actor.get().id());
    }
    dh.setStaff(com.example.jewelrystore.util.Checks.get(customers, actor.get().id()));
    dh.setStatus(next);
    deliveries.transition(dh);
    if (next == OrderStatus.DA_XAC_NHAN) dh.setConfirmedAt(Instant.now());
    if (next == OrderStatus.HOAN_THANH) {
      dh.setCompletedAt(Instant.now());
      paymentService.completeCod(dh);
    }
    return orderMapper.order(dh);
  }
}
