package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.*;

public record CustomerUpdateRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})?$") String phone) {}
