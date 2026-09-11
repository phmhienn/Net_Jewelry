package com.example.jewelrystore.dto.response;

import java.math.BigDecimal;

public record PaymentInstructionResponse(
    String bankCode,
    String accountNumber,
    String accountName,
    BigDecimal amount,
    String content,
    String qrUrl) {}
