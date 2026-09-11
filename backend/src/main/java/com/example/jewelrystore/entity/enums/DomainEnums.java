package com.example.jewelrystore.entity.enums;

public final class DomainEnums {
  private DomainEnums() {}

  public enum Role {
    KHACH_HANG,
    NHAN_VIEN,
    QUAN_LY
  }

  public enum AccountStatus {
    HOAT_DONG,
    KHOA
  }

  public enum CategoryStatus {
    HOAT_DONG,
    NGUNG_HOAT_DONG
  }

  public enum ProductStatus {
    DANG_BAN,
    NGUNG_BAN
  }

  public enum StockAction {
    DIEU_CHINH,
    GIAI_PHONG,
    GIAO_HANG,
    GIU_HANG,
    NHAP,
    XUAT
  }

  public enum CouponType {
    PHAN_TRAM,
    SO_TIEN
  }

  public enum CouponStatus {
    HOAT_DONG,
    NGUNG
  }

  public enum OrderStatus {
    CHO_XAC_NHAN,
    DA_XAC_NHAN,
    DANG_XU_LY,
    DANG_GIAO_HANG,
    HOAN_THANH,
    DA_HUY
  }

  public enum PaymentMethod {
    COD,
    BANK_TRANSFER,
    ONLINE
  }

  public enum PaymentStatus {
    PENDING,
    CONFIRMED,
    FAILED
  }

  public enum TransactionStatus {
    THANH_CONG,
    THAT_BAI
  }

  public enum DeliveryStatus {
    CHO_XU_LY,
    DANG_GIAO,
    DA_GIAO,
    THAT_BAI,
    HOAN_TRA
  }

  public enum ReviewStatus {
    HIEN_THI,
    AN
  }

  public enum ContentType {
    FAQ,
    CHINH_SACH,
    GIOI_THIEU,
    LIEN_HE,
    TRANG_CHU
  }
}
