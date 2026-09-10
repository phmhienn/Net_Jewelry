package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.DeliveryStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record DeliveryResponse(
    Long id,
    Long orderId,
    String carrier,
    String trackingCode,
    BigDecimal shipping,
    Instant expectedAt,
    Instant shippedAt,
    Instant deliveredAt,
    DeliveryStatus status,
    String note) {}
