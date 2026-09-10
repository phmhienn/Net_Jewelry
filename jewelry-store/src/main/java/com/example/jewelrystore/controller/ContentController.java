package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.enums.DomainEnums.ContentType;
import com.example.jewelrystore.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContentController {
  private final ContentService service;

  @GetMapping("/api/banners")
  public ApiResponse<PageResponse<BannerResponse>> banners(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.banners(false, page, size));
  }

  @GetMapping("/api/admin/banners")
  public ApiResponse<PageResponse<BannerResponse>> adminBanners(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.banners(true, page, size));
  }

  @PostMapping("/api/admin/banners")
  public ApiResponse<BannerResponse> createBanner(@Valid @RequestBody BannerRequest request) {
    return ApiResponse.ok(service.saveBanner(null, request));
  }

  @PutMapping("/api/admin/banners/{id}")
  public ApiResponse<BannerResponse> updateBanner(
      @PathVariable Long id, @Valid @RequestBody BannerRequest request) {
    return ApiResponse.ok(service.saveBanner(id, request));
  }

  @DeleteMapping("/api/admin/banners/{id}")
  public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
    service.deleteBanner(id);
    return ApiResponse.ok(null);
  }

  @GetMapping("/api/contents")
  public ApiResponse<PageResponse<ContentResponse>> contents(
      @RequestParam(required = false) ContentType type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.contents(false, type, page, size));
  }

  @GetMapping("/api/contents/{slug}")
  public ApiResponse<ContentResponse> content(@PathVariable String slug) {
    return ApiResponse.ok(service.content(slug));
  }

  @GetMapping("/api/admin/contents")
  public ApiResponse<PageResponse<ContentResponse>> adminContents(
      @RequestParam(required = false) ContentType type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.contents(true, type, page, size));
  }

  @PostMapping("/api/admin/contents")
  public ApiResponse<ContentResponse> createContent(@Valid @RequestBody ContentRequest request) {
    return ApiResponse.ok(service.saveContent(null, request));
  }

  @PutMapping("/api/admin/contents/{id}")
  public ApiResponse<ContentResponse> updateContent(
      @PathVariable Long id, @Valid @RequestBody ContentRequest request) {
    return ApiResponse.ok(service.saveContent(id, request));
  }

  @DeleteMapping("/api/admin/contents/{id}")
  public ApiResponse<Void> deleteContent(@PathVariable Long id) {
    service.deleteContent(id);
    return ApiResponse.ok(null);
  }
}
