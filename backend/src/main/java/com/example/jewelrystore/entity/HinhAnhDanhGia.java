package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hinh_anh_danh_gia")
public class HinhAnhDanhGia extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "danh_gia_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_anh_danh_gia"))
  private DanhGia review;

  @Column(name = "duong_dan", nullable = false, length = 1000)
  private String url;

  @Column(name = "thu_tu", nullable = false)
  private int sortOrder = 0;
}
