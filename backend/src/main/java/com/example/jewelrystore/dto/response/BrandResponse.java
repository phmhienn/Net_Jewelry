package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record BrandResponse(
    Long id,
    String name,
    String logo,
    String description,
    com.example.jewelrystore.entity.enums.DomainEnums.CategoryStatus status) {}
