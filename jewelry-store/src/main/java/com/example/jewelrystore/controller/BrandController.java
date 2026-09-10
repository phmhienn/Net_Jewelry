package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.BrandService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {
  private final BrandService service;

  @GetMapping
  public ApiResponse<List<BrandResponse>> list() {
    return ApiResponse.ok(service.brands());
  }

  @GetMapping("/{id}")
  public ApiResponse<BrandResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.brand(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
    return ApiResponse.ok(service.saveBrand(null, request));
  }

  @PutMapping("/{id}")
  public ApiResponse<BrandResponse> update(
      @PathVariable Long id, @Valid @RequestBody BrandRequest request) {
    return ApiResponse.ok(service.saveBrand(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.deleteBrand(id);
    return ApiResponse.done();
  }
}
