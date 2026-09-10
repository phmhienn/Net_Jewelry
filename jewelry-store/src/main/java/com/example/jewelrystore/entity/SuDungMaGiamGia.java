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
    name = "su_dung_ma_giam_gia",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_su_dung_ma_don_hang",
          columnNames = {"don_hang_id"})
    })
@org.hibernate.annotations.Check(name = "ck_su_dung_ma", constraints = "so_tien_giam >= 0")
public class SuDungMaGiamGia extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "ma_giam_gia_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_su_dung_ma"))
  private MaGiamGia coupon;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_su_dung_ma_tai_khoan"))
  private TaiKhoan customer;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "don_hang_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_su_dung_ma_don_hang"))
  private DonHang order;

  @Column(name = "so_tien_giam", nullable = false, precision = 19, scale = 2)
  private BigDecimal discount;

  @Column(name = "thoi_gian_su_dung", nullable = false)
  private Instant usedAt = Instant.now();
}
