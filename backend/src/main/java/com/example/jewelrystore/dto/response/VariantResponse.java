package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record VariantResponse(
    Long id,
    String sku,
    String size,
    String color,
    BigDecimal price,
    int stock,
    int reserved,
    int available,
    BigDecimal salePrice,
    ProductStatus status) {}
