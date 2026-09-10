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
@RequestMapping("/api/admin/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {
  private final AddressService service;

  @GetMapping
  public ApiResponse<List<AddressResponse>> list(@PathVariable Long customerId) {
    return ApiResponse.ok(service.addresses(customerId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<AddressResponse> create(
      @PathVariable Long customerId, @Valid @RequestBody AddressRequest request) {
    return ApiResponse.ok(service.saveAddress(customerId, null, request));
  }

  @PutMapping("/{id}")
  public ApiResponse<AddressResponse> update(
      @PathVariable Long customerId,
      @PathVariable Long id,
      @Valid @RequestBody AddressRequest request) {
    return ApiResponse.ok(service.saveAddress(customerId, id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long customerId, @PathVariable Long id) {
    service.deleteAddress(customerId, id);
    return ApiResponse.done();
  }

  @PutMapping("/{id}/default")
  public ApiResponse<AddressResponse> primary(
      @PathVariable Long customerId, @PathVariable Long id) {
    return ApiResponse.ok(service.defaultAddress(customerId, id));
  }
}
