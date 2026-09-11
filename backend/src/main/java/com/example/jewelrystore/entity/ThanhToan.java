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
    name = "thanh_toan",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_thanh_toan_don_hang",
          columnNames = {"don_hang_id"})
    })
@org.hibernate.annotations.Check(name = "ck_thanh_toan", constraints = "so_tien > 0")
public class ThanhToan extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "don_hang_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_thanh_toan_don_hang"))
  private DonHang order;

  @Column(name = "so_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "phuong_thuc", nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "thoi_gian_thanh_toan", nullable = true)
  private Instant paidAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
