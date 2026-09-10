package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record StaffLoginRequest(
    @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 72) String password) {}
