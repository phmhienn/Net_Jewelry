package com.example.jewelrystore.dto.request;

import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.validation.constraints.*;

public record AddressRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @NotBlank @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})$") String phone,
    @NotBlank @Size(max = 255) String city,
    @NotBlank @Size(max = 255) String district,
    @NotBlank @Size(max = 255) String ward,
    @NotBlank @Size(min = 5, max = 500) String street,
    Boolean defaultAddress) {
  public AddressRequest {
    defaultAddress = Boolean.TRUE.equals(defaultAddress);
  }
}
