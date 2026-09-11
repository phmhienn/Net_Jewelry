package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "don_hang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_don_hang_ma",
          columnNames = {"ma_don_hang"}),
      @UniqueConstraint(
          name = "uk_don_hang_idempotency",
          columnNames = {"tai_khoan_id", "idempotency_key"})
    })
@org.hibernate.annotations.Check(
    name = "ck_don_hang_tien",
    constraints =
        "tong_tien > 0 AND tien_giam >= 0 AND phi_van_chuyen >= 0 AND tong_thanh_toan = tong_tien - tien_giam + phi_van_chuyen AND tong_thanh_toan >= 0")
public class DonHang extends BaseEntity {
  @Column(name = "ma_don_hang", nullable = false, length = 50)
  private String code;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_don_hang_tai_khoan"))
  private TaiKhoan customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(
      name = "nhan_vien_xu_ly_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_don_hang_nhan_vien"))
  private TaiKhoan staff;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(
      name = "ma_giam_gia_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_don_hang_ma_giam_gia"))
  private MaGiamGia coupon;

  @Column(name = "ngay_dat", nullable = false)
  private Instant date = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private OrderStatus status = OrderStatus.CHO_XAC_NHAN;

  @Column(name = "tong_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "tien_giam", nullable = false, precision = 19, scale = 2)
  private BigDecimal discount = BigDecimal.ZERO;

  @Column(name = "phi_van_chuyen", nullable = false, precision = 19, scale = 2)
  private BigDecimal shipping = BigDecimal.ZERO;

  @Column(name = "tong_thanh_toan", nullable = false, precision = 19, scale = 2)
  private BigDecimal total;

  @Column(name = "ho_ten_nguoi_nhan", nullable = false, length = 120)
  private String recipientName;

  @Column(name = "so_dien_thoai_nguoi_nhan", nullable = false, length = 20)
  private String recipientPhone;

  @Column(name = "dia_chi_giao_hang", nullable = false, length = 3000)
  private String addressSnapshot;

  @Column(name = "phuong_thuc_van_chuyen", nullable = false, length = 100)
  private String shippingMethod;

  @Column(name = "ghi_chu", nullable = true, length = 1000)
  private String note;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Column(name = "ngay_xac_nhan", nullable = true)
  private Instant confirmedAt;

  @Column(name = "ngay_hoan_thanh", nullable = true)
  private Instant completedAt;

  @Column(name = "ngay_huy", nullable = true)
  private Instant cancelledAt;

  @Column(name = "ly_do_huy", nullable = true, length = 500)
  private String cancelReason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
