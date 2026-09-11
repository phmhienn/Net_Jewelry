package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CouponController {
  private final CouponService service;

  @PostMapping("/api/coupons/quote")
  public ApiResponse<CheckoutQuoteResponse> quote(@Valid @RequestBody CouponQuoteRequest request) {
    return ApiResponse.ok(service.quote(request.code()));
  }

  @GetMapping("/api/admin/coupons")
  public ApiResponse<PageResponse<CouponResponse>> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(page, size));
  }

  @PostMapping("/api/admin/coupons")
  public ApiResponse<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
    return ApiResponse.ok(service.save(null, request));
  }

  @PutMapping("/api/admin/coupons/{id}")
  public ApiResponse<CouponResponse> update(
      @PathVariable Long id, @Valid @RequestBody CouponRequest request) {
    return ApiResponse.ok(service.save(id, request));
  }

  @DeleteMapping("/api/admin/coupons/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.disable(id);
    return ApiResponse.ok(null);
  }
}
