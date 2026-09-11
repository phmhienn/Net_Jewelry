package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record CartItemRequest(@NotNull @Positive Long variantId, @Min(1) @Max(1000) int quantity) {}
