package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
  private final AddressService service;

  @GetMapping
  public ApiResponse<List<AddressResponse>> list() {
    return ApiResponse.ok(service.addresses(null));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
    return ApiResponse.ok(service.saveAddress(null, null, request));
  }

  @PutMapping("/{id}")
  public ApiResponse<AddressResponse> update(
      @PathVariable Long id, @Valid @RequestBody AddressRequest request) {
    return ApiResponse.ok(service.saveAddress(null, id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id) {
    service.deleteAddress(null, id);
    return ApiResponse.done();
  }

  @PutMapping("/{id}/default")
  public ApiResponse<AddressResponse> primary(@PathVariable Long id) {
    return ApiResponse.ok(service.defaultAddress(null, id));
  }
}
