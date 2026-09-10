package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
    @NotBlank @Size(max = 200) String token,
    @NotBlank @Size(min = 8, max = 72) String newPassword) {}
