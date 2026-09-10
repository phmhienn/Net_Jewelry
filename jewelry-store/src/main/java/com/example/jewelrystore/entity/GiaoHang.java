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
    name = "giao_hang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_giao_hang_don_hang",
          columnNames = {"don_hang_id"}),
      @UniqueConstraint(
          name = "uk_giao_hang_ma_van_don",
          columnNames = {"ma_van_don"})
    })
@org.hibernate.annotations.Check(name = "ck_giao_hang_phi", constraints = "phi_van_chuyen >= 0")
public class GiaoHang extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "don_hang_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_giao_hang_don_hang"))
  private DonHang order;

  @Column(name = "don_vi_van_chuyen", nullable = true, length = 150)
  private String carrier;

  @Column(name = "ma_van_don", nullable = true, length = 100)
  private String trackingCode;

  @Column(name = "phi_van_chuyen", nullable = false, precision = 19, scale = 2)
  private BigDecimal shipping = BigDecimal.ZERO;

  @Column(name = "ngay_du_kien", nullable = true)
  private Instant expectedAt;

  @Column(name = "ngay_giao", nullable = true)
  private Instant shippedAt;

  @Column(name = "ngay_nhan", nullable = true)
  private Instant deliveredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private DeliveryStatus status = DeliveryStatus.CHO_XU_LY;

  @Column(name = "ghi_chu", nullable = true, length = 500)
  private String note;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
