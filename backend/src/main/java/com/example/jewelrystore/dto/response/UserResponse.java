package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record UserResponse(
    Long id,
    String name,
    String email,
    String phone,
    String avatar,
    Role role,
    AccountStatus status,
    Instant createdAt) {}
