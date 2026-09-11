package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponResponse(
    Long id,
    String code,
    String name,
    CouponType type,
    BigDecimal value,
    BigDecimal minimumOrder,
    BigDecimal maximumDiscount,
    Integer quantity,
    int usedCount,
    Instant startsAt,
    Instant endsAt,
    CouponStatus status) {}
