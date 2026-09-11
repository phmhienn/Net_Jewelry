package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.time.*;
import java.util.*;

public record StaffResponse(
    Long id,
    String name,
    String email,
    String phone,
    String username,
    Role role,
    AccountStatus status,
    Instant createdAt) {}
