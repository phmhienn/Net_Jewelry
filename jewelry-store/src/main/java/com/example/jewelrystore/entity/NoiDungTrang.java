package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "noi_dung_trang",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_noi_dung_slug",
          columnNames = {"slug"})
    })
public class NoiDungTrang extends BaseEntity {
  @Enumerated(EnumType.STRING)
  @Column(name = "loai_noi_dung", nullable = false)
  private ContentType type;

  @Column(name = "tieu_de", nullable = false, length = 255)
  private String title;

  @Column(name = "slug", nullable = false, length = 255)
  private String slug;

  @Column(name = "noi_dung", nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private ReviewStatus status = ReviewStatus.HIEN_THI;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
