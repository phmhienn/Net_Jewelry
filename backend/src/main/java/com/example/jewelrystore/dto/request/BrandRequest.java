package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record BrandRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String logo,
    @Size(max = 2000) String description,
    com.example.jewelrystore.entity.enums.DomainEnums.CategoryStatus status) {}
