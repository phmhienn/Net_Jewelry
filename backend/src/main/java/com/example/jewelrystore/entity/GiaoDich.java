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
    name = "giao_dich",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_giao_dich_ma",
          columnNames = {"ma_giao_dich_thanh_toan"})
    })
@org.hibernate.annotations.Check(name = "ck_giao_dich_so_tien", constraints = "so_tien > 0")
public class GiaoDich extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "thanh_toan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_giao_dich_thanh_toan"))
  private ThanhToan payment;

  @Column(name = "ma_giao_dich_thanh_toan", nullable = false, length = 100)
  private String code;

  @Column(name = "so_tien", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "phuong_thuc", nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private TransactionStatus status;

  @Column(name = "thoi_gian", nullable = false)
  private Instant time = Instant.now();

  @Column(name = "ghi_chu", nullable = true, length = 500)
  private String note;
}
