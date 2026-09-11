package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ApiResponse.ok(service.register(request));
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ApiResponse.ok(service.login(request));
  }

  @PostMapping("/staff/login")
  @io.swagger.v3.oas.annotations.Operation(
      deprecated = true,
      description = "Tương thích ứng dụng cũ. Dùng POST /api/auth/login cho cả ba vai trò.")
  public ApiResponse<LoginResponse> staffLogin(@Valid @RequestBody StaffLoginRequest request) {
    return ApiResponse.ok(service.staffLogin(request));
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> me() {
    return ApiResponse.ok(service.me());
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout() {
    service.logout();
    return ApiResponse.done();
  }

  @PutMapping("/password")
  public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordRequest request) {
    service.changePassword(request);
    return ApiResponse.done();
  }

  @PostMapping("/forgot-password")
  public ApiResponse<Void> forgot(@Valid @RequestBody ForgotPasswordRequest request) {
    service.forgotPassword(request);
    return new ApiResponse<>(true, "Nếu email tồn tại, hướng dẫn đã được gửi", null);
  }

  @PostMapping("/reset-password")
  public ApiResponse<Void> reset(@Valid @RequestBody ResetPasswordRequest request) {
    service.resetPassword(request);
    return ApiResponse.done();
  }
}
