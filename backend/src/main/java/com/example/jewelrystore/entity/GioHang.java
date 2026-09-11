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
    name = "gio_hang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_gio_hang_tai_khoan",
          columnNames = {"tai_khoan_id"})
    })
@org.hibernate.annotations.Check(name = "ck_gio_hang_tong_tien", constraints = "tong_tien >= 0")
public class GioHang extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_gio_hang_tai_khoan"))
  private TaiKhoan customer;

  @Column(name = "tong_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "ngay_tao", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "ngay_cap_nhat", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
