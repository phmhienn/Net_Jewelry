package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record OrderResponse(
    Long id,
    Long customerId,
    Instant date,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total,
    String note,
    AddressResponse address,
    String shippingMethod,
    OrderStatus status,
    List<OrderLineResponse> items,
    PaymentResponse payment,
    String code,
    BigDecimal discount,
    String couponCode,
    DeliveryResponse delivery) {}
