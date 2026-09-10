package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UploadController {
  private final UploadService service;

  @PostMapping(value = "/api/products/{id}/images/upload", consumes = "multipart/form-data")
  @PreAuthorize("hasAnyRole('NHAN_VIEN','QUAN_LY')")
  public ApiResponse<ImageResponse> product(
      @PathVariable Long id,
      @RequestPart MultipartFile file,
      @RequestParam(defaultValue = "false") boolean primary) {
    return ApiResponse.ok(service.product(id, file, primary));
  }

  @PostMapping(value = "/api/reviews/{id}/images/upload", consumes = "multipart/form-data")
  public ApiResponse<ImageResponse> review(@PathVariable Long id, @RequestPart MultipartFile file) {
    return ApiResponse.ok(service.review(id, file));
  }

  @PostMapping(value = "/api/account/avatar/upload", consumes = "multipart/form-data")
  public ApiResponse<UserResponse> avatar(@RequestPart MultipartFile file) {
    return ApiResponse.ok(service.avatar(file));
  }
}
