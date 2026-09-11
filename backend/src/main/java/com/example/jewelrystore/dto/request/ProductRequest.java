package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 100) String sku,
    @NotBlank @Size(max = 200) String name,
    @NotNull @Positive Long categoryId,
    @NotNull @Positive Long brandId,
    @Size(max = 5000) String description,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal price,
    @Size(max = 50) String size,
    @Size(max = 80) String color,
    @NotBlank @Size(max = 100) String material,
    @PositiveOrZero @Digits(integer = 9, fraction = 3) BigDecimal weight,
    @Size(max = 100) String gemstone,
    @NotNull ProductStatus status,
    @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal salePrice) {}
