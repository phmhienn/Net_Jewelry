package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.ReviewStatus;
import java.time.Instant;

public record BannerResponse(
    Long id,
    String title,
    String image,
    String link,
    int sortOrder,
    ReviewStatus status,
    Instant startsAt,
    Instant endsAt) {}
