package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService service;

  @GetMapping
  public ApiResponse<List<CategoryResponse>> list(
      @RequestParam(defaultValue = "false") boolean tree) {
    return ApiResponse.ok(service.categories(tree));
  }

  @GetMapping("/{id}")
  public ApiResponse<CategoryResponse> get(@PathVariable Long id) {
    return ApiResponse.ok(service.category(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
    return ApiResponse.ok(service.saveCategory(null, request));
  }

  @PutMapping("/{id}")
  public ApiResponse<CategoryResponse> update(
      @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    return ApiResponse.ok(service.saveCategory(id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.deleteCategory(id);
    return ApiResponse.done();
  }
}
