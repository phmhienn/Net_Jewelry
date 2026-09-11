package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record PasswordRequest(
    @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 72) String newPassword) {}
