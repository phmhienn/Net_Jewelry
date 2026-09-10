package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Size(max = 190) @JsonAlias({"email", "username"}) String identifier,
    @NotBlank @Size(max = 72) String password) {}
