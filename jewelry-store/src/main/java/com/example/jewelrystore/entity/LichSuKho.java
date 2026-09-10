package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lich_su_kho")
@org.hibernate.annotations.Check(
    name = "ck_lich_su_kho",
    constraints = "ton_sau >= 0 AND dat_sau >= 0 AND dat_sau <= ton_sau")
public class LichSuKho extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "bien_the_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_lich_su_kho_bien_the"))
  private BienTheSanPham variant;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_lich_su_kho_tai_khoan"))
  private TaiKhoan account;

  @Enumerated(EnumType.STRING)
  @Column(name = "loai", nullable = false)
  private StockAction action;

  @Column(name = "thay_doi_ton", nullable = false)
  private int quantityChange = 0;

  @Column(name = "thay_doi_dat", nullable = false)
  private int reservedChange = 0;

  @Column(name = "ton_sau", nullable = false)
  private int quantityAfter;

  @Column(name = "dat_sau", nullable = false)
  private int reservedAfter;

  @Column(name = "ly_do", nullable = false, length = 500)
  private String reason;

  @Column(name = "thoi_gian", nullable = false)
  private Instant createdAt = Instant.now();
}
