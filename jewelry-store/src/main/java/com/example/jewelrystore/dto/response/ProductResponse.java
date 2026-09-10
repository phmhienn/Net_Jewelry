package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    Long categoryId,
    String category,
    Long brandId,
    String brand,
    String description,
    BigDecimal price,
    String size,
    String color,
    String material,
    BigDecimal weight,
    String gemstone,
    ProductStatus status,
    Instant createdAt,
    List<ImageResponse> images,
    List<VariantResponse> variants,
    double rating,
    long reviews,
    BigDecimal salePrice) {}
