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
    name = "ma_giam_gia",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_ma_giam_gia_code",
          columnNames = {"ma_code"})
    })
@org.hibernate.annotations.Check(name = "ck_ma_giam_gia_gia_tri", constraints = "gia_tri_giam > 0")
@org.hibernate.annotations.Check(
    name = "ck_ma_giam_gia_don_toi_thieu",
    constraints = "don_hang_toi_thieu >= 0")
@org.hibernate.annotations.Check(
    name = "ck_ma_giam_gia_so_luong",
    constraints = "so_luong IS NULL OR so_luong >= 0")
@org.hibernate.annotations.Check(
    name = "ck_ma_giam_gia_thoi_gian",
    constraints = "ngay_ket_thuc > ngay_bat_dau")
public class MaGiamGia extends BaseEntity {
  @Column(name = "ma_code", nullable = false, length = 50)
  private String code;

  @Column(name = "ten_ma", nullable = false, length = 150)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "loai_giam", nullable = false)
  private CouponType type;

  @Column(name = "gia_tri_giam", nullable = false, precision = 19, scale = 2)
  private BigDecimal value;

  @Column(name = "don_hang_toi_thieu", nullable = false, precision = 19, scale = 2)
  private BigDecimal minimumOrder = BigDecimal.ZERO;

  @Column(name = "giam_toi_da", nullable = true, precision = 19, scale = 2)
  private BigDecimal maximumDiscount;

  @Column(name = "so_luong", nullable = true)
  private Integer quantity;

  @Column(name = "so_luong_da_dung", nullable = false)
  private int usedCount = 0;

  @Column(name = "ngay_bat_dau", nullable = false)
  private Instant startsAt;

  @Column(name = "ngay_ket_thuc", nullable = false)
  private Instant endsAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private CouponStatus status = CouponStatus.HOAT_DONG;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
