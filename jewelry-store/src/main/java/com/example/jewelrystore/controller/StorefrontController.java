package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StorefrontController {
  private final ShippingService service;

  @GetMapping("/api/storefront/settings")
  public ApiResponse<StorefrontSettingsResponse> settings() {
    return ApiResponse.ok(service.settings());
  }
}
