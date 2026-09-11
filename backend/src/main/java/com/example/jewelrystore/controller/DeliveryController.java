package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.DeliveryRequest;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DeliveryController {
  private final DeliveryService service;

  @PutMapping("/api/admin/orders/{id}/delivery")
  public ApiResponse<DeliveryResponse> update(
      @PathVariable Long id, @Valid @RequestBody DeliveryRequest request) {
    return ApiResponse.ok(service.update(id, request));
  }
}
