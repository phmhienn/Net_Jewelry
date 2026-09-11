package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record StaffUpdateRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})?$") String phone) {}
