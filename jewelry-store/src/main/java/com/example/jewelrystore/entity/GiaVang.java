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
    name = "gia_vang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_gia_vang_loai_ngay",
          columnNames = {"loai_vang", "ngay_ap_dung"})
    })
@org.hibernate.annotations.Check(
    name = "ck_gia_vang",
    constraints = "gia_mua > 0 AND gia_ban >= gia_mua")
public class GiaVang extends BaseEntity {
  @Column(name = "loai_vang", nullable = false, length = 80)
  private String type;

  @Column(name = "don_vi", nullable = false, length = 30)
  private String unit;

  @Column(name = "ngay_ap_dung", nullable = false)
  private LocalDate date;

  @Column(name = "gia_mua", nullable = false, precision = 19, scale = 2)
  private BigDecimal buyPrice;

  @Column(name = "gia_ban", nullable = false, precision = 19, scale = 2)
  private BigDecimal sellPrice;

  @Column(name = "ngay_cap_nhat", nullable = false)
  private Instant updatedAt = Instant.now();
}
