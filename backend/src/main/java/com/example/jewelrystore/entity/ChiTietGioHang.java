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
    name = "chi_tiet_gio_hang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_chi_tiet_gio_hang",
          columnNames = {"gio_hang_id", "bien_the_id"})
    })
@org.hibernate.annotations.Check(
    name = "ck_ct_gio_hang",
    constraints = "so_luong BETWEEN 1 AND 1000 AND don_gia > 0 AND thanh_tien = so_luong * don_gia")
public class ChiTietGioHang extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "gio_hang_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ct_gio_hang"))
  private GioHang cart;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "bien_the_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ct_gio_hang_bien_the"))
  private BienTheSanPham variant;

  @Column(name = "so_luong", nullable = false)
  private int quantity = 1;

  @Column(name = "don_gia", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "thanh_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal total;

  @Column(name = "da_chon", nullable = false)
  private boolean selected;
}
