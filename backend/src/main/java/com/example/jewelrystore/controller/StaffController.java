package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class StaffController {
  private final StaffService service;

  @GetMapping
  public ApiResponse<PageResponse<StaffResponse>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(keyword, page, size));
  }

  @GetMapping("/{id}")
  public ApiResponse<StaffResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.get(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
    return ApiResponse.ok(service.create(request));
  }

  @PutMapping("/{id}")
  public ApiResponse<StaffResponse> update(
      @PathVariable Long id, @Valid @RequestBody StaffUpdateRequest request) {
    return ApiResponse.ok(service.update(id, request));
  }

  @PatchMapping("/{id}/role")
  public ApiResponse<StaffResponse> role(
      @PathVariable Long id, @Valid @RequestBody RoleRequest request) {
    return ApiResponse.ok(service.role(id, request));
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<StaffResponse> status(
      @PathVariable Long id, @Valid @RequestBody AccountStatusRequest request) {
    return ApiResponse.ok(service.status(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ApiResponse.done();
  }
}
