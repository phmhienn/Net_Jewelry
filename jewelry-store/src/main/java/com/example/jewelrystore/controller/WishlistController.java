package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {
  private final WishlistService service;

  @GetMapping
  public ApiResponse<PageResponse<ProductResponse>> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<Boolean> contains(@PathVariable Long id) {
    return ApiResponse.ok(service.contains(id));
  }

  @PutMapping("/{id}")
  public ApiResponse<Void> add(@PathVariable Long id) {
    service.add(id);
    return ApiResponse.ok(null);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> remove(@PathVariable Long id) {
    service.remove(id);
    return ApiResponse.ok(null);
  }
}
