package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record ReviewResponse(
    Long id,
    Long productId,
    Long customerId,
    String customerName,
    int stars,
    String content,
    Instant createdAt,
    Instant updatedAt,
    ReviewStatus status,
    String reply,
    List<ImageResponse> images) {}
