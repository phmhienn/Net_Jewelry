package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @Pattern(regexp = "[A-Za-z0-9_.-]{3,100}") String username) {}
