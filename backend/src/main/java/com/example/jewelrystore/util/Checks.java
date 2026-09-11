package com.example.jewelrystore.util;

import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.repository.BaseRepository;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class Checks {
  private Checks() {}

  public static <T extends BaseEntity> T get(BaseRepository<T> repo, Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu: " + id));
  }

  public static <T extends BaseEntity> T lock(BaseRepository<T> repo, Long id) {
    return repo.findLockedById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu: " + id));
  }

  public static void require(boolean condition, String message) {
    if (!condition) throw new BadRequestException(message);
  }

  public static void owner(Long actual, Long expected) {
    if (!actual.equals(expected))
      throw new ForbiddenException("Bạn không có quyền truy cập dữ liệu này");
  }

  public static void password(String value) {
    require(value.getBytes(StandardCharsets.UTF_8).length <= 72, "Mật khẩu tối đa 72 byte");
  }

  public static String hash(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static String imageUrl(String value) {
    if (value == null || value.isBlank()) return null;
    if (value.matches("/uploads/[a-f0-9-]+\\.(jpg|png|webp)")) return value;
    try {
      URI uri = URI.create(value);
      if ("https".equals(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null)
        return value;
    } catch (Exception ignored) {
    }
    throw new BadRequestException("Ảnh phải là đường dẫn upload hoặc URL HTTPS");
  }

  public static void sellable(BienTheSanPham btsp) {
    require(
        btsp.getStatus() == ProductStatus.DANG_BAN
            && btsp.getEffectivePrice().signum() > 0
            && btsp.getProduct().getBrand().getStatus() == CategoryStatus.HOAT_DONG
            && btsp.getProduct().getStatus() == ProductStatus.DANG_BAN
            && btsp.getProduct().getCategory().getStatus() == CategoryStatus.HOAT_DONG,
        "Sản phẩm đã ngừng bán");
  }
}
