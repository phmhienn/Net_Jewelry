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
    name = "thuong_hieu",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_thuong_hieu_ten",
          columnNames = {"ten_thuong_hieu"})
    })
public class ThuongHieu extends BaseEntity {
  @Column(name = "ten_thuong_hieu", nullable = false, length = 120)
  private String name;

  @Column(name = "logo", nullable = true, length = 1000)
  private String logo;

  @Column(name = "mo_ta", nullable = true, length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private CategoryStatus status = CategoryStatus.HOAT_DONG;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
