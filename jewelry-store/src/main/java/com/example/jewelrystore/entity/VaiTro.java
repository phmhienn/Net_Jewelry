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
    name = "vai_tro",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_vai_tro_ten",
          columnNames = {"ten_vai_tro"})
    })
public class VaiTro extends BaseEntity {
  @Column(name = "ten_vai_tro", nullable = false, length = 50)
  private String name;

  @Column(name = "mo_ta", nullable = true, length = 255)
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
