package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dia_chi")
public class DiaChi extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_dia_chi_tai_khoan"))
  private TaiKhoan customer;

  @Column(name = "ho_ten_nguoi_nhan", nullable = false, length = 120)
  private String name;

  @Column(name = "so_dien_thoai", nullable = false, length = 20)
  private String phone;

  @Column(name = "tinh_thanh", nullable = false, length = 255)
  private String city;

  @Column(name = "quan_huyen", nullable = false, length = 255)
  private String district;

  @Column(name = "phuong_xa", nullable = false, length = 255)
  private String ward;

  @Column(name = "dia_chi_chi_tiet", nullable = false, length = 500)
  private String street;

  @Column(name = "mac_dinh", nullable = false)
  private boolean defaultAddress;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
