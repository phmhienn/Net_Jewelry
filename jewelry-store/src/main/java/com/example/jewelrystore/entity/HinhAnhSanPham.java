package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hinh_anh_san_pham")
public class HinhAnhSanPham extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "san_pham_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_anh_san_pham"))
  private SanPham product;

  @Column(name = "duong_dan", nullable = false, length = 1000)
  private String url;

  @Column(name = "la_anh_chinh", nullable = false)
  private boolean primaryImage;

  @Column(name = "thu_tu", nullable = false)
  private int sortOrder = 0;
}
