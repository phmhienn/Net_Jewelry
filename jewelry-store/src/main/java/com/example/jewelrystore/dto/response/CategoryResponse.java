package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record CategoryResponse(
    Long id,
    String name,
    Long parentId,
    String description,
    CategoryStatus status,
    List<CategoryResponse> children) {}
