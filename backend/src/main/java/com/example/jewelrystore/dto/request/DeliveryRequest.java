package com.example.jewelrystore.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record DeliveryRequest(
    @NotBlank @Size(max = 150) String carrier,
    @NotBlank @Size(max = 100) String trackingCode,
    Instant expectedAt,
    @Size(max = 500) String note) {}
