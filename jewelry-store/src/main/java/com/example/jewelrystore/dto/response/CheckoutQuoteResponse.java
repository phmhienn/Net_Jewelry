package com.example.jewelrystore.dto.response;

import java.math.BigDecimal;

public record CheckoutQuoteResponse(
    String code, BigDecimal subtotal, BigDecimal discount, BigDecimal shipping, BigDecimal total) {}
