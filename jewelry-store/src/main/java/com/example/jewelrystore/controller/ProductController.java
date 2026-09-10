package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {
  @GetMapping("/api/products/materials")
  public ApiResponse<List<String>> materials() {
    return ApiResponse.ok(service.materials());
  }

  private final ProductService service;

  @GetMapping("/api/products")
  public ApiResponse<PageResponse<ProductResponse>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long category,
      @RequestParam(required = false) Long brand,
      @RequestParam(required = false) String material,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "newest") String sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(
        service.products(
            keyword, category, brand, material, minPrice, maxPrice, sort, page, size, false));
  }

  @GetMapping("/api/admin/products")
  public ApiResponse<PageResponse<ProductResponse>> admin(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(
        service.products(keyword, null, null, null, null, null, "newest", page, size, true));
  }

  @GetMapping("/api/products/{id}")
  public ApiResponse<ProductResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.product(id, false));
  }

  @GetMapping("/api/admin/products/{id}")
  public ApiResponse<ProductResponse> adminGet(@PathVariable Long id) {
    return ApiResponse.ok(service.product(id, true));
  }

  @PostMapping("/api/products")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
    return ApiResponse.ok(service.saveProduct(null, request));
  }

  @PutMapping("/api/products/{id}")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<ProductResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    return ApiResponse.ok(service.saveProduct(id, request));
  }

  @DeleteMapping("/api/products/{id}")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.deleteProduct(id);
    return ApiResponse.done();
  }

  @GetMapping("/api/products/{id}/images")
  public ApiResponse<List<ImageResponse>> images(@PathVariable Long id) {
    return ApiResponse.ok(service.images(id));
  }

  @PostMapping("/api/products/{id}/images")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ImageResponse> image(
      @PathVariable Long id, @Valid @RequestBody ImageRequest request) {
    return ApiResponse.ok(service.addImage(id, request));
  }

  @PutMapping("/api/product-images/{id}/primary")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<ImageResponse> primary(@PathVariable Long id) {
    return ApiResponse.ok(service.primaryImage(id));
  }

  @DeleteMapping("/api/product-images/{id}")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<Void> deleteImage(@PathVariable Long id) {
    service.deleteImage(id);
    return ApiResponse.done();
  }

  @GetMapping("/api/products/{id}/variants")
  public ApiResponse<List<VariantResponse>> variants(@PathVariable Long id) {
    return ApiResponse.ok(service.variants(id));
  }

  @PostMapping("/api/products/{id}/variants")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<VariantResponse> variant(
      @PathVariable Long id, @Valid @RequestBody VariantRequest request) {
    return ApiResponse.ok(service.saveVariant(id, null, request));
  }

  @PutMapping("/api/variants/{id}")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<VariantResponse> updateVariant(
      @PathVariable Long id, @Valid @RequestBody VariantRequest request) {
    return ApiResponse.ok(service.saveVariant(null, id, request));
  }

  @DeleteMapping("/api/variants/{id}")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<Void> deleteVariant(@PathVariable Long id) {
    service.deleteVariant(id);
    return ApiResponse.done();
  }
}
