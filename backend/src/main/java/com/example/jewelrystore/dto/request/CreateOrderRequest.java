package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateOrderRequest(
    @NotNull @Valid AddressRequest address,
    @NotNull PaymentMethod payment,
    @Size(max = 1000) String note,
    @Size(max = 50) String couponCode) {}
