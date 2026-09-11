package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record InventoryResponse(
    Long variantId,
    String sku,
    String productName,
    int quantity,
    int reserved,
    int available,
    boolean lowStock,
    Instant updatedAt,
    int lowStockThreshold) {}
