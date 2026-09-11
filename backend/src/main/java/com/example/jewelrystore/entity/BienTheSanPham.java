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
    name = "bien_the_san_pham",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_bien_the_sku",
          columnNames = {"sku"})
    })
@org.hibernate.annotations.Check(name = "ck_bien_the_gia", constraints = "gia > 0")
@org.hibernate.annotations.Check(
    name = "ck_bien_the_gia_km",
    constraints = "gia_khuyen_mai IS NULL OR ( gia_khuyen_mai >= 0 AND gia_khuyen_mai <= gia )")
public class BienTheSanPham extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "san_pham_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_bien_the_san_pham"))
  private SanPham product;

  @Column(name = "sku", nullable = false, length = 100)
  private String sku;

  @Column(name = "size", nullable = true, length = 50)
  private String size;

  @Column(name = "mau_sac", nullable = true, length = 80)
  private String color;

  @Column(name = "gia", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Column(name = "gia_khuyen_mai", nullable = true, precision = 19, scale = 2)
  private BigDecimal salePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private ProductStatus status = ProductStatus.DANG_BAN;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public BigDecimal getEffectivePrice() {
    return salePrice == null ? price : salePrice;
  }

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
