package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;

  @GetMapping("/api/payments/{id}")
  public ApiResponse<PaymentResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.get(id));
  }

  @GetMapping("/api/orders/{orderId}/payment-status")
  public ApiResponse<PaymentStatusResponse> statusByOrder(@PathVariable Long orderId) {
    return ApiResponse.ok(service.statusByOrder(orderId));
  }

  @PostMapping("/api/admin/payments/{id}/confirm")
  public ApiResponse<PaymentResponse> confirm(@PathVariable Long id) {
    return ApiResponse.ok(service.confirm(id));
  }

  @GetMapping("/api/admin/payments")
  public ApiResponse<PageResponse<PaymentResponse>> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(page, size));
  }

  @GetMapping("/api/admin/transactions")
  public ApiResponse<PageResponse<TransactionResponse>> transactions(
      @RequestParam(required = false) Long paymentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.transactions(paymentId, page, size));
  }
}
