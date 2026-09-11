package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record TransactionResponse(
    Long id,
    Long paymentId,
    Long orderId,
    String code,
    BigDecimal amount,
    PaymentMethod method,
    Instant time,
    TransactionStatus status) {}
