package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.response.StorefrontSettingsResponse;
import java.math.BigDecimal;

public interface ShippingService {
  BigDecimal fee(BigDecimal subtotal);

  StorefrontSettingsResponse settings();
}
