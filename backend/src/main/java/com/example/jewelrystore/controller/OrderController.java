package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {
  private final OrderService service;

  @PostMapping("/api/orders")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<OrderResponse> create(
      @Valid @RequestBody CreateOrderRequest request,
      @RequestHeader("Idempotency-Key") String key) {
    return ApiResponse.ok(service.create(request, key));
  }

  @GetMapping("/api/orders")
  public ApiResponse<PageResponse<OrderResponse>> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(page, size));
  }

  @GetMapping({"/api/orders/{id}", "/api/admin/orders/{id}"})
  public ApiResponse<OrderResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.get(id));
  }

  @PostMapping("/api/orders/{id}/cancel")
  public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
    return ApiResponse.ok(service.cancel(id));
  }

  @GetMapping("/api/admin/orders")
  public ApiResponse<PageResponse<OrderResponse>> admin(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.adminList(status, customerId, keyword, date, page, size));
  }

  @PatchMapping("/api/admin/orders/{id}/status")
  public ApiResponse<OrderResponse> status(
      @PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
    return ApiResponse.ok(service.status(id, request));
  }
}
