package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.*;
import com.example.jewelrystore.util.Pages;
import java.math.*;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponServiceImpl implements CouponService {
  private final MaGiamGiaRepository coupons;
  private final SuDungMaGiamGiaRepository uses;
  private final CartService carts;
  private final ShippingService shipping;
  private final CurrentActor actor;

  public CouponResponse response(MaGiamGia mgg) {
    return new CouponResponse(
        mgg.getId(),
        mgg.getCode(),
        mgg.getName(),
        mgg.getType(),
        mgg.getValue(),
        mgg.getMinimumOrder(),
        mgg.getMaximumDiscount(),
        mgg.getQuantity(),
        mgg.getUsedCount(),
        mgg.getStartsAt(),
        mgg.getEndsAt(),
        mgg.getStatus());
  }

  @Transactional(readOnly = true)
  public PageResponse<CouponResponse> list(int page, int size) {
    return PageResponse.of(coupons.findAll(Pages.of(page, size)).map(this::response));
  }

  public CouponResponse save(Long id, CouponRequest request) {
    require(request.endsAt().isAfter(request.startsAt()), "Ngày kết thúc phải sau ngày bắt đầu");
    require(
        request.type() != CouponType.PHAN_TRAM
            || request.value().compareTo(new BigDecimal("100")) <= 0,
        "Phần trăm giảm tối đa 100");
    var mgg = id == null ? new MaGiamGia() : lock(coupons, id);
    String code = request.code().trim().toUpperCase(Locale.ROOT);
    coupons
        .findByCode(code)
        .ifPresent(existing -> require(existing.getId().equals(id), "Mã giảm giá đã tồn tại"));
    require(
        request.quantity() == null || request.quantity() >= mgg.getUsedCount(),
        "Số lượng không được nhỏ hơn số đã dùng");
    mgg.setCode(code);
    mgg.setName(request.name());
    mgg.setType(request.type());
    mgg.setValue(request.value());
    mgg.setMinimumOrder(request.minimumOrder());
    mgg.setMaximumDiscount(request.maximumDiscount());
    mgg.setQuantity(request.quantity());
    mgg.setStartsAt(request.startsAt());
    mgg.setEndsAt(request.endsAt());
    mgg.setStatus(request.status());
    return response(coupons.save(mgg));
  }

  public void disable(Long id) {
    lock(coupons, id).setStatus(CouponStatus.NGUNG);
  }

  private MaGiamGia find(String code) {
    return coupons
        .findByCode(code.trim().toUpperCase(Locale.ROOT))
        .orElseThrow(
            () ->
                new com.example.jewelrystore.exception.BadRequestException(
                    "Mã giảm giá không tồn tại"));
  }

  private BigDecimal discount(MaGiamGia mgg, BigDecimal subtotal) {
    var now = Instant.now();
    require(
        mgg.getStatus() == CouponStatus.HOAT_DONG
            && !now.isBefore(mgg.getStartsAt())
            && now.isBefore(mgg.getEndsAt()),
        "Mã giảm giá chưa có hiệu lực hoặc đã hết hạn");
    require(
        mgg.getQuantity() == null || mgg.getUsedCount() < mgg.getQuantity(),
        "Mã giảm giá đã hết lượt sử dụng");
    require(
        subtotal.compareTo(mgg.getMinimumOrder()) >= 0,
        "Đơn hàng chưa đạt giá trị tối thiểu của mã");
    require(
        mgg.getValue().signum() > 0
            && (mgg.getType() != CouponType.PHAN_TRAM
                || mgg.getValue().compareTo(new BigDecimal("100")) <= 0),
        "Giá trị mã giảm giá không hợp lệ");
    BigDecimal amount =
        mgg.getType() == CouponType.PHAN_TRAM
            ? subtotal.multiply(mgg.getValue()).divide(new BigDecimal("100"), 2, RoundingMode.DOWN)
            : mgg.getValue();
    if (mgg.getMaximumDiscount() != null) {
      require(mgg.getMaximumDiscount().signum() > 0, "Giới hạn giảm không hợp lệ");
      amount = amount.min(mgg.getMaximumDiscount());
    }
    return amount.min(subtotal);
  }

  public CheckoutQuoteResponse quote(String code) {
    actor.customerId();
    var gh = carts.get();
    BigDecimal subtotal =
        gh.items().stream()
            .filter(CartItemResponse::selected)
            .map(CartItemResponse::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    require(subtotal.signum() > 0, "Vui lòng chọn sản phẩm trong giỏ hàng");
    var mgg = find(code);
    var amount = discount(mgg, subtotal);
    var fee = shipping.fee(subtotal);
    require(subtotal.subtract(amount).add(fee).signum() > 0, "Tổng thanh toán phải lớn hơn 0");
    return new CheckoutQuoteResponse(
        mgg.getCode(), subtotal, amount, fee, subtotal.subtract(amount).add(fee));
  }

  public void apply(DonHang dh, String code) {
    if (code == null || code.isBlank()) return;
    // Fetch under the lock directly: a preceding non-locking read can cache a stale quota.
    var mgg =
        coupons
            .lockByCode(code.trim().toUpperCase(Locale.ROOT))
            .orElseThrow(
                () ->
                    new com.example.jewelrystore.exception.BadRequestException(
                        "Mã giảm giá không tồn tại"));
    dh.setDiscount(discount(mgg, dh.getSubtotal()));
    dh.setCoupon(mgg);
    mgg.setUsedCount(mgg.getUsedCount() + 1);
  }

  public void record(DonHang dh) {
    if (dh.getCoupon() == null) return;
    var sd = new SuDungMaGiamGia();
    sd.setOrder(dh);
    sd.setCustomer(dh.getCustomer());
    sd.setCoupon(dh.getCoupon());
    sd.setDiscount(dh.getDiscount());
    uses.save(sd);
  }

  public void release(DonHang dh) {
    if (dh.getCoupon() == null) return;
    var mgg = lock(coupons, dh.getCoupon().getId());
    mgg.setUsedCount(Math.max(0, mgg.getUsedCount() - 1));
    // Keep usage history on cancelled orders; only restore the available quota.
  }
}
