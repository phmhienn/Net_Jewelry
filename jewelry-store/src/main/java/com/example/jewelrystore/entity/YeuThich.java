package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "yeu_thich")
@IdClass(YeuThichId.class)
public class YeuThich {
  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_yeu_thich_tai_khoan"))
  private TaiKhoan customer;

  @Id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "san_pham_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_yeu_thich_san_pham"))
  private SanPham product;

  @Column(name = "ngay_them", nullable = false)
  private Instant addedAt = Instant.now();
}
