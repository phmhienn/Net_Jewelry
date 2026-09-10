package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService service;

  @GetMapping
  public ApiResponse<CartResponse> get() {
    return ApiResponse.ok(service.get());
  }

  @PostMapping("/items")
  public ApiResponse<CartResponse> add(@Valid @RequestBody CartItemRequest request) {
    return ApiResponse.ok(service.add(request));
  }

  @PutMapping("/items/{id}")
  public ApiResponse<CartResponse> update(
      @PathVariable Long id, @Valid @RequestBody QuantityRequest request) {
    return ApiResponse.ok(service.update(id, request));
  }

  @DeleteMapping("/items/{id}")
  public ApiResponse<CartResponse> remove(@PathVariable Long id) {
    return ApiResponse.ok(service.remove(id));
  }

  @PatchMapping("/items/{id}/selection")
  public ApiResponse<CartResponse> select(
      @PathVariable Long id, @Valid @RequestBody SelectionRequest request) {
    return ApiResponse.ok(service.select(id, request));
  }

  @DeleteMapping
  public ApiResponse<CartResponse> clear() {
    return ApiResponse.ok(service.clear());
  }
}
