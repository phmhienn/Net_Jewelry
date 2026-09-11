package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.*;

public record CouponQuoteRequest(@NotBlank @Size(max = 50) String code) {}
