package com.example.jewelrystore.entity;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "danh_gia")
@org.hibernate.annotations.Check(
    name = "ck_danh_gia_so_sao",
    constraints = "so_sao BETWEEN 1 AND 5")
public class DanhGia extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "san_pham_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_danh_gia_san_pham"))
  private SanPham product;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "tai_khoan_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_danh_gia_tai_khoan"))
  private TaiKhoan customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(
      name = "don_hang_id",
      nullable = true,
      foreignKey = @ForeignKey(name = "fk_danh_gia_don_hang"))
  private DonHang order;

  @JdbcTypeCode(SqlTypes.TINYINT)
  @Column(name = "so_sao", nullable = false)
  private byte stars;

  @Column(name = "noi_dung", nullable = false, length = 3000)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "trang_thai", nullable = false)
  private ReviewStatus status = ReviewStatus.HIEN_THI;

  @Column(name = "phan_hoi", nullable = true, length = 3000)
  private String reply;

  @Column(name = "ngay_tao", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "ngay_cap_nhat", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }
}
