package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoldPriceRequest(
    @NotBlank @Size(max = 80) String type,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal buyPrice,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal sellPrice,
    @NotNull @PastOrPresent LocalDate date,
    @NotBlank @Size(max = 30) String unit) {}
