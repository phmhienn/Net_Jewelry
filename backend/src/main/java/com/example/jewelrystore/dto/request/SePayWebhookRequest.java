package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.*;

public record SePayWebhookRequest(
    @NotNull Long id,
    String gateway,
    String transactionDate,
    String accountNumber,
    String subAccount,
    String code,
    String content,
    String transferType,
    @NotNull Long transferAmount,
    Long accumulated,
    String referenceCode,
    String description) {}
