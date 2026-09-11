package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "banner")
public class Banner extends BaseEntity {
  @Column(name = "tieu_de", nullable = true, length = 200)
  private String title;

  @Column(name = "hinh_anh", nullable = false, length = 1000)
  private String image;

  @Column(name = "duong_dan_lien_ket", nullable = true, length = 1000)
  private String link;

  @Column(name = "thu_tu", nullable = false)
  private int sortOrder = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private ReviewStatus status = ReviewStatus.HIEN_THI;

  @Column(name = "ngay_bat_dau", nullable = true)
  private Instant startsAt;

  @Column(name = "ngay_ket_thuc", nullable = true)
  private Instant endsAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
