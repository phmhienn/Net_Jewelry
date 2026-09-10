package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.GoldPriceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gold-prices")
@RequiredArgsConstructor
public class GoldPriceController {
  private final GoldPriceService service;

  @GetMapping("/current")
  public ApiResponse<List<GoldPriceResponse>> current() {
    return ApiResponse.ok(service.current());
  }

  @GetMapping
  public ApiResponse<PageResponse<GoldPriceResponse>> history(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.history(type, from, to, page, size));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<GoldPriceResponse> create(@Valid @RequestBody GoldPriceRequest request) {
    return ApiResponse.ok(service.save(null, request));
  }

  @PutMapping("/{id}")
  public ApiResponse<GoldPriceResponse> update(
      @PathVariable Long id, @Valid @RequestBody GoldPriceRequest request) {
    return ApiResponse.ok(service.save(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ApiResponse.done();
  }
}
