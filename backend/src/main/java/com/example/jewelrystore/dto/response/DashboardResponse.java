package com.example.jewelrystore.dto.response;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record DashboardResponse(
    long totalOrders,
    long completedOrders,
    long cancelledOrders,
    long shippingOrders,
    long products,
    long customers,
    BigDecimal revenue) {}
