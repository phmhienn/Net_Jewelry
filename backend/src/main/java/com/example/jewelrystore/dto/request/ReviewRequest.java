package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ReviewRequest(
    @Min(1) @Max(5) int stars, @NotBlank @Size(max = 3000) String content) {}
