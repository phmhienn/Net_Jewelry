package com.example.jewelrystore.service.impl;

import com.example.jewelrystore.config.SePayConfig;
import com.example.jewelrystore.exception.UnauthorizedException;
import com.example.jewelrystore.service.SePaySignatureService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class SePaySignatureServiceImpl implements SePaySignatureService {
  private static final long MAX_SKEW_SECONDS = 300;
  private final SePayConfig config;

  public SePaySignatureServiceImpl(SePayConfig config) {
    this.config = config;
  }

  public void verify(String rawBody, String signature, String timestamp) {
    if (config.webhookSecret().isBlank()) {
      throw new UnauthorizedException("Webhook SePay chưa được cấu hình");
    }
    if (signature == null || timestamp == null || signature.isBlank() || timestamp.isBlank()) {
      throw new UnauthorizedException("Thiếu chữ ký SePay");
    }
    long signedAt;
    try {
      signedAt = Long.parseLong(timestamp.trim());
    } catch (NumberFormatException e) {
      throw new UnauthorizedException("Timestamp SePay không hợp lệ");
    }
    if (Math.abs(Instant.now().getEpochSecond() - signedAt) > MAX_SKEW_SECONDS) {
      throw new UnauthorizedException("Webhook SePay đã quá hạn");
    }
    String expected = "sha256=" + hmac(timestamp.trim() + "." + rawBody, config.webhookSecret());
    if (!constantTimeEquals(expected, signature.trim())) {
      throw new UnauthorizedException("Chữ ký SePay không hợp lệ");
    }
  }

  private String hmac(String data, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) hex.append(String.format("%02x", b));
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Không thể xác thực webhook SePay", e);
    }
  }

  private boolean constantTimeEquals(String a, String b) {
    return java.security.MessageDigest.isEqual(
        a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
  }
}
