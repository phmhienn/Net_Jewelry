package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "ton_kho",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_ton_kho_bien_the",
          columnNames = {"bien_the_id"})
    })
@org.hibernate.annotations.Check(
    name = "ck_ton_kho",
    constraints = "so_luong_ton >= 0 AND so_luong_dat >= 0 AND so_luong_dat <= so_luong_ton")
public class TonKho extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "bien_the_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_ton_kho_bien_the"))
  private BienTheSanPham variant;

  @Column(name = "so_luong_ton", nullable = false)
  private int quantity = 0;

  @Column(name = "so_luong_dat", nullable = false)
  private int reserved = 0;

  @Column(name = "nguong_canh_bao", nullable = false)
  private int lowStockThreshold = 5;

  @Column(name = "ngay_cap_nhat", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
