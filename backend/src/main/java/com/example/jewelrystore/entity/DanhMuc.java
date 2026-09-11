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
    name = "danh_muc",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_danh_muc_ten",
          columnNames = {"ten_danh_muc"})
    })
public class DanhMuc extends BaseEntity {
  @Column(name = "ten_danh_muc", nullable = false, length = 120)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(
      name = "danh_muc_cha_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_danh_muc_cha"))
  private DanhMuc parent;

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
