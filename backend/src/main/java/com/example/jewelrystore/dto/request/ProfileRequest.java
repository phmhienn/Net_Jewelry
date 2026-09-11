package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ProfileRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})?$") String phone) {}
