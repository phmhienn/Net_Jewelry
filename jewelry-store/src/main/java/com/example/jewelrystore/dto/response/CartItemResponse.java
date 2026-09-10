package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record CartItemResponse(
    Long id,
    Long variantId,
    Long productId,
    String productName,
    String sku,
    String size,
    String color,
    String image,
    String material,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal total,
    int available,
    boolean selected) {}
