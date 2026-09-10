package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;

public interface OrderService {
  OrderResponse create(CreateOrderRequest request, String key);

  PageResponse<OrderResponse> list(int page, int size);

  PageResponse<OrderResponse> adminList(
      String status, Long customerId, String keyword, String date, int page, int size);

  OrderResponse get(Long id);

  OrderResponse cancel(Long id);

  OrderResponse status(Long id, OrderStatusRequest request);
}
