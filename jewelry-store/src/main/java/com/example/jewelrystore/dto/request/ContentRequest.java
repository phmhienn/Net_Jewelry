package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ContentRequest(
    @NotNull ContentType type,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 255) String slug,
    @NotBlank @Size(max = 100000) String content,
    @NotNull ReviewStatus status) {}
