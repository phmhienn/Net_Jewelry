package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record StockHistoryResponse(
    Long id,
    Long variantId,
    Long accountId,
    StockAction action,
    int quantityChange,
    int reservedChange,
    int quantityAfter,
    int reservedAfter,
    String reason,
    Instant createdAt) {}
