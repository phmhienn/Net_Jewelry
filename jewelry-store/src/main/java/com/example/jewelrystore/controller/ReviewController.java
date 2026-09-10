package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService service;

  @GetMapping("/api/products/{productId}/reviews")
  public ApiResponse<PageResponse<ReviewResponse>> list(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(productId, false, page, size));
  }

  @PostMapping("/api/products/{productId}/reviews")
  @PreAuthorize("hasRole('KHACH_HANG')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ReviewResponse> create(
      @PathVariable Long productId, @Valid @RequestBody ReviewRequest request) {
    return ApiResponse.ok(service.create(productId, request));
  }

  @PutMapping("/api/reviews/{id}")
  public ApiResponse<ReviewResponse> update(
      @PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
    return ApiResponse.ok(service.update(id, request));
  }

  @DeleteMapping("/api/reviews/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.delete(id, false);
    return ApiResponse.done();
  }

  @PostMapping("/api/reviews/{id}/images")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ImageResponse> image(
      @PathVariable Long id, @Valid @RequestBody ImageRequest request) {
    return ApiResponse.ok(service.addImage(id, request.url()));
  }

  @DeleteMapping("/api/review-images/{id}")
  public ApiResponse<Void> deleteImage(@PathVariable Long id) {
    service.deleteImage(id);
    return ApiResponse.done();
  }

  @GetMapping("/api/admin/reviews")
  public ApiResponse<PageResponse<ReviewResponse>> admin(
      @RequestParam(required = false) Long productId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.list(productId, true, page, size));
  }

  @PutMapping("/api/admin/reviews/{id}/reply")
  public ApiResponse<ReviewResponse> reply(
      @PathVariable Long id, @Valid @RequestBody ReplyRequest request) {
    return ApiResponse.ok(service.reply(id, request));
  }

  @PatchMapping("/api/admin/reviews/{id}/status")
  public ApiResponse<ReviewResponse> status(
      @PathVariable Long id, @Valid @RequestBody ReviewStatusRequest request) {
    return ApiResponse.ok(service.status(id, request));
  }

  @DeleteMapping("/api/admin/reviews/{id}")
  public ApiResponse<Void> deleteAdmin(@PathVariable Long id) {
    service.delete(id, true);
    return ApiResponse.done();
  }
}
