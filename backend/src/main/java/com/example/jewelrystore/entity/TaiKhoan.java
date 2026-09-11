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
    name = "tai_khoan",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_tai_khoan_email",
          columnNames = {"email"}),
      @UniqueConstraint(
          name = "uk_tai_khoan_ten_dang_nhap",
          columnNames = {"ten_dang_nhap"}),
      @UniqueConstraint(
          name = "uk_tai_khoan_reset_token",
          columnNames = {"reset_token_hash"})
    })
@org.hibernate.annotations.Check(
    name = "ck_tai_khoan_token_version",
    constraints = "token_version >= 0")
public class TaiKhoan extends BaseEntity {
  @Column(name = "ho_ten", nullable = false, length = 120)
  private String name;

  @Column(name = "email", nullable = false, length = 190)
  private String email;

  @Column(name = "so_dien_thoai", nullable = true, length = 20)
  private String phone;

  @Column(name = "ten_dang_nhap", nullable = false, length = 100)
  private String username;

  @Column(name = "mat_khau", nullable = false, length = 255)
  private String password;

  @Column(name = "avatar", nullable = true, length = 500)
  private String avatar;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(
      name = "vai_tro_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_tai_khoan_vai_tro"))
  private VaiTro authority;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private AccountStatus status = AccountStatus.HOAT_DONG;

  @Column(name = "token_version", nullable = false)
  private long tokenVersion;

  @Column(name = "reset_token_hash", nullable = true, length = 64)
  private String resetTokenHash;

  @Column(name = "reset_token_expires_at", nullable = true)
  private Instant resetTokenExpiresAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public Role getRole() {
    return Role.valueOf(authority.getName());
  }

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
