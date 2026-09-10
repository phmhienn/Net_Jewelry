package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.enums.DomainEnums.StockAction;
import com.example.jewelrystore.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
  private final InventoryService service;

  @GetMapping
  public ApiResponse<PageResponse<InventoryResponse>> list(
      @RequestParam(defaultValue = "false") boolean lowStock,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(lowStock, keyword, page, size));
  }

  @GetMapping("/{variantId}")
  public ApiResponse<InventoryResponse> get(@PathVariable Long variantId) {
    return ApiResponse.ok(service.get(variantId));
  }

  @GetMapping("/{variantId}/history")
  public ApiResponse<PageResponse<StockHistoryResponse>> history(
      @PathVariable Long variantId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.history(variantId, page, size));
  }

  @PatchMapping("/{variantId}/threshold")
  public ApiResponse<InventoryResponse> threshold(
      @PathVariable Long variantId, @Valid @RequestBody StockThresholdRequest request) {
    return ApiResponse.ok(service.threshold(variantId, request));
  }

  @PostMapping("/import")
  public ApiResponse<InventoryResponse> add(@Valid @RequestBody InventoryRequest request) {
    return ApiResponse.ok(service.change(request, StockAction.NHAP));
  }

  @PostMapping("/export")
  public ApiResponse<InventoryResponse> subtract(@Valid @RequestBody InventoryRequest request) {
    return ApiResponse.ok(service.change(request, StockAction.XUAT));
  }

  @PostMapping("/adjust")
  public ApiResponse<InventoryResponse> adjust(@Valid @RequestBody InventoryRequest request) {
    return ApiResponse.ok(service.change(request, StockAction.DIEU_CHINH));
  }
}
