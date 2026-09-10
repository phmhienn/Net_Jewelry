package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;

public record ContentResponse(
    Long id, ContentType type, String title, String slug, String content, ReviewStatus status) {}
