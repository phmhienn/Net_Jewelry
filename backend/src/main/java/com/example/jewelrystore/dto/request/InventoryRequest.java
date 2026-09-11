package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record InventoryRequest(
    @NotNull @Positive Long variantId,
    @Min(0) @Max(1000000) int quantity,
    @NotBlank @Size(max = 500) String reason) {}
