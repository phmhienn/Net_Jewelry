package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,50}") String code,
    @NotBlank @Size(max = 150) String name,
    @NotNull CouponType type,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal value,
    @NotNull @PositiveOrZero BigDecimal minimumOrder,
    @Positive BigDecimal maximumDiscount,
    @PositiveOrZero Integer quantity,
    @NotNull Instant startsAt,
    @NotNull Instant endsAt,
    @NotNull CouponStatus status) {}
