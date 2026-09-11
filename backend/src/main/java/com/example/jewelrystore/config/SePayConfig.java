package com.example.jewelrystore.config;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SePayConfig {
  private final String apiKey;
  private final String webhookSecret;
  private final String accountNumber;
  private final String bankCode;
  private final String accountName;

  public SePayConfig(
      @Value("${sepay.api-key:}") String apiKey,
      @Value("${sepay.webhook-secret:}") String webhookSecret,
      @Value("${sepay.account-number:}") String accountNumber,
      @Value("${sepay.bank-code:}") String bankCode,
      @Value("${sepay.account-name:}") String accountName) {
    this.apiKey = apiKey == null ? "" : apiKey.trim();
    this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    this.accountNumber = accountNumber == null ? "" : accountNumber.trim();
    this.bankCode = bankCode == null ? "" : bankCode.trim();
    this.accountName = accountName == null ? "" : accountName.trim();
  }

  public String apiKey() { return apiKey; }
  public String webhookSecret() { return webhookSecret; }
  public String accountNumber() { return accountNumber; }
  public String bankCode() { return bankCode; }
  public String accountName() { return accountName; }

  public boolean bankConfigured() {
    return !accountNumber.isBlank() && !bankCode.isBlank() && !accountName.isBlank();
  }

  public String vietQrUrl(BigDecimal amount, String content) {
    if (!bankConfigured()) return null;
    var query =
        "amount=" + amount.toBigInteger().toString()
            + "&addInfo=" + encode(content)
            + "&accountName=" + encode(accountName);
    return "https://img.vietqr.io/image/" + encode(bankCode) + "-" + encode(accountNumber)
        + "-compact2.png?" + query;
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
