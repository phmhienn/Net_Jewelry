package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;

public record PaymentStatusResponse(
    Long orderId,
    String orderCode,
    PaymentStatus paymentStatus,
    PaymentMethod paymentMethod,
    BigDecimal amount) {}
