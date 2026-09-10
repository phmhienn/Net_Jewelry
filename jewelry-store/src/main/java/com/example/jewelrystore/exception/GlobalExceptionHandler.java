package com.example.jewelrystore.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  ResponseEntity<?> missing(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(404)
        .body(body(404, "Không tìm thấy tài nguyên", r.getRequestURI()));
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  ResponseEntity<?> method(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(405)
        .body(body(405, "HTTP method không được hỗ trợ", r.getRequestURI()));
  }

  @ExceptionHandler(org.springframework.dao.PessimisticLockingFailureException.class)
  ResponseEntity<?> locked(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(409)
        .body(body(409, "Dữ liệu đang được cập nhật. Vui lòng thử lại", r.getRequestURI()));
  }

  public record ErrorResponse(
      Instant timestamp,
      int status,
      String message,
      String path,
      Map<String, String> fieldErrors) {}

  public static ErrorResponse body(int code, String message, String path) {
    return new ErrorResponse(Instant.now(), code, message, path, Map.of());
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> api(ApiException e, HttpServletRequest r) {
    return ResponseEntity.status(e.getStatus())
        .body(body(e.getStatus(), e.getMessage(), r.getRequestURI()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> fields = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(
                Instant.now(), 400, "Dữ liệu không hợp lệ", r.getRequestURI(), fields));
  }

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    jakarta.validation.ConstraintViolationException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.web.bind.MissingRequestHeaderException.class
  })
  ResponseEntity<?> invalid(Exception e, HttpServletRequest r) {
    return ResponseEntity.badRequest()
        .body(body(400, "Request thiếu trường hoặc có giá trị không hợp lệ", r.getRequestURI()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(409)
        .body(body(409, "Dữ liệu bị trùng hoặc đang được sử dụng", r.getRequestURI()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> forbidden(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(403)
        .body(body(403, "Bạn không có quyền thực hiện thao tác này", r.getRequestURI()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> upload(Exception e, HttpServletRequest r) {
    return ResponseEntity.status(413)
        .body(body(413, "Ảnh vượt quá dung lượng cho phép", r.getRequestURI()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unknown(Exception e, HttpServletRequest r) {
    org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed: {}", r.getRequestURI(), e);
    return ResponseEntity.internalServerError()
        .body(body(500, "Lỗi hệ thống. Vui lòng thử lại.", r.getRequestURI()));
  }
}
