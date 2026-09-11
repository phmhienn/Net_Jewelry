package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record CategoryRequest(
    @NotBlank @Size(max = 120) String name,
    @Positive Long parentId,
    @Size(max = 2000) String description,
    @NotNull CategoryStatus status) {}
