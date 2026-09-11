package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerService service;

  @GetMapping("/api/admin/customers")
  public ApiResponse<PageResponse<UserResponse>> customers(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.customers(keyword, page, size));
  }

  @GetMapping("/api/admin/customers/{id}")
  public ApiResponse<CustomerSummaryResponse> customer(@PathVariable Long id) {
    return ApiResponse.ok(service.customer(id));
  }

  @PostMapping("/api/admin/customers")
  public ApiResponse<UserResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
    return ApiResponse.ok(service.createCustomer(request));
  }

  @PutMapping("/api/admin/customers/{id}")
  public ApiResponse<UserResponse> updateCustomer(
      @PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
    return ApiResponse.ok(service.updateCustomer(id, request));
  }

  @PatchMapping("/api/admin/customers/{id}/status")
  public ApiResponse<UserResponse> status(
      @PathVariable Long id, @Valid @RequestBody AccountStatusRequest request) {
    return ApiResponse.ok(service.customerStatus(id, request));
  }

  @DeleteMapping("/api/admin/customers/{id}")
  public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
    service.deleteCustomer(id);
    return ApiResponse.ok(null);
  }
}
