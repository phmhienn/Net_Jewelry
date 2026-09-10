package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
  private final UserService service;

  @GetMapping("/api/account/profile")
  public ApiResponse<UserResponse> profile() {
    return ApiResponse.ok(service.profile());
  }

  @PutMapping("/api/account/profile")
  public ApiResponse<UserResponse> update(@Valid @RequestBody ProfileRequest request) {
    return ApiResponse.ok(service.update(request));
  }

  @PutMapping("/api/account/avatar")
  public ApiResponse<UserResponse> avatar(@Valid @RequestBody ImageRequest request) {
    return ApiResponse.ok(service.avatar(request.url()));
  }

  @DeleteMapping("/api/account/avatar")
  public ApiResponse<UserResponse> deleteAvatar() {
    return ApiResponse.ok(service.avatar(null));
  }
}
