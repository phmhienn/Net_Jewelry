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
@Table(name = "chi_tiet_don_hang")
@org.hibernate.annotations.Check(
    name = "ck_ct_don_hang",
    constraints = "so_luong BETWEEN 1 AND 1000 AND don_gia > 0 AND thanh_tien = so_luong * don_gia")
public class ChiTietDonHang extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "don_hang_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ct_don_hang"))
  private DonHang order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "bien_the_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ct_don_hang_bien_the"))
  private BienTheSanPham variant;

  @Column(name = "ten_san_pham_chot", nullable = false, length = 200)
  private String productName;

  @Column(name = "sku_chot", nullable = false, length = 100)
  private String sku;

  @Column(name = "size_chot", nullable = true, length = 50)
  private String size;

  @Column(name = "mau_sac_chot", nullable = true, length = 80)
  private String color;

  @Column(name = "so_luong", nullable = false)
  private int quantity;

  @Column(name = "don_gia", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "thanh_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal total;
}
