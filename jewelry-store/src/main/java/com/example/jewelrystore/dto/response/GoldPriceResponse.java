package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record GoldPriceResponse(
    Long id,
    String type,
    BigDecimal buyPrice,
    BigDecimal sellPrice,
    LocalDate date,
    String unit,
    Instant updatedAt) {}
