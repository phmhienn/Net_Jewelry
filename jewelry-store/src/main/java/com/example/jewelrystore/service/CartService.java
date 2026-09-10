package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;

public interface CartService {
  CartResponse select(Long id, com.example.jewelrystore.dto.request.SelectionRequest request);

  CartResponse get();

  CartResponse add(CartItemRequest request);

  CartResponse update(Long id, QuantityRequest request);

  CartResponse remove(Long id);

  CartResponse clear();
}
