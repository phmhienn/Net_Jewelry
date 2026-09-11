package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record VariantRequest(
    @NotBlank @Size(max = 100) String sku,
    @Size(max = 50) String size,
    @Size(max = 80) String color,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal price,
    @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal salePrice,
    ProductStatus status) {}
