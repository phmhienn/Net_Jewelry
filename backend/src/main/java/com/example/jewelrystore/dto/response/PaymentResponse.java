package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record PaymentResponse(
    Long id,
    Long orderId,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    Instant paidAt,
    PaymentInstructionResponse instruction) {}
