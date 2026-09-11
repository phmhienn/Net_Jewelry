package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.*;

public record StockThresholdRequest(@NotNull @PositiveOrZero Integer threshold) {}
