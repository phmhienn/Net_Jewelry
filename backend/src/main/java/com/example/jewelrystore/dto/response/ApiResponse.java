package com.example.jewelrystore.dto.response;

public record ApiResponse<T>(boolean success, String message, T data) {
  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "Thành công", data);
  }

  public static ApiResponse<Void> done() {
    return ok(null);
  }
}
