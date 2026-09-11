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
    name = "san_pham",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_san_pham_sku",
          columnNames = {"ma_sku"})
    })
@org.hibernate.annotations.Check(name = "ck_san_pham_gia", constraints = "gia > 0")
@org.hibernate.annotations.Check(
    name = "ck_san_pham_gia_km",
    constraints = "gia_khuyen_mai IS NULL OR ( gia_khuyen_mai >= 0 AND gia_khuyen_mai <= gia )")
@org.hibernate.annotations.Check(
    name = "ck_san_pham_trong_luong",
    constraints = "trong_luong IS NULL OR trong_luong >= 0")
public class SanPham extends BaseEntity {
  @Column(name = "ma_sku", nullable = false, length = 100)
  private String sku;

  @Column(name = "ten_san_pham", nullable = false, length = 200)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "danh_muc_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_san_pham_danh_muc"))
  private DanhMuc category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "thuong_hieu_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_san_pham_thuong_hieu"))
  private ThuongHieu brand;

  @Column(name = "mo_ta", nullable = true, length = 5000)
  private String description;

  @Column(name = "gia", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(name = "gia_khuyen_mai", nullable = true, precision = 19, scale = 2)
  private BigDecimal salePrice;

  @Column(name = "size", nullable = true, length = 50)
  private String size;

  @Column(name = "mau_sac", nullable = true, length = 80)
  private String color;

  @Column(name = "chat_lieu", nullable = false, length = 100)
  private String material;

  @Column(name = "trong_luong", nullable = true, precision = 12, scale = 3)
  private BigDecimal weight;

  @Column(name = "da_quy", nullable = true, length = 100)
  private String gemstone;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private ProductStatus status = ProductStatus.DANG_BAN;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @org.hibernate.annotations.Formula(
      "coalesce((select min(coalesce(bt.gia_khuyen_mai, bt.gia)) from bien_the_san_pham bt where bt.san_pham_id = id and bt.trang_thai = 'DANG_BAN'), coalesce(gia_khuyen_mai, gia))")
  private BigDecimal displayPrice;

  public BigDecimal getEffectivePrice() {
    return salePrice == null ? price : salePrice;
  }

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
