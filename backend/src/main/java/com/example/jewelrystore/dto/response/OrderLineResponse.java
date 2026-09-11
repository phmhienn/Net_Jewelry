package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record OrderLineResponse(
    Long id,
    Long variantId,
    Long productId,
    String productName,
    String sku,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal total,
    String size,
    String color) {}
