package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.ReviewStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record BannerRequest(
    @Size(max = 200) String title,
    @NotBlank @Size(max = 1000) String image,
    @Size(max = 1000) String link,
    @PositiveOrZero Integer sortOrder,
    @NotNull ReviewStatus status,
    Instant startsAt,
    Instant endsAt) {
  public BannerRequest {
    if (sortOrder == null) sortOrder = 0;
  }
}
