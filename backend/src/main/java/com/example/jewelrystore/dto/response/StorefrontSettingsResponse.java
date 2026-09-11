package com.example.jewelrystore.dto.response;

import java.math.BigDecimal;

public record StorefrontSettingsResponse(
    BigDecimal shippingFee, BigDecimal freeShippingThreshold) {}
