package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record ImageRequest(
    @NotBlank @Size(max = 1000) String url,
    Boolean primaryImage,
    @PositiveOrZero Integer sortOrder) {
  public ImageRequest {
    primaryImage = Boolean.TRUE.equals(primaryImage);
  }
}
