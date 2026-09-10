package com.example.jewelrystore.service.impl;

import com.example.jewelrystore.dto.response.StorefrontSettingsResponse;
import com.example.jewelrystore.service.ShippingService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShippingServiceImpl implements ShippingService {
  private final BigDecimal shippingFee, freeThreshold;

  public ShippingServiceImpl(
      @Value("${app.shipping.fee}") BigDecimal fee,
      @Value("${app.shipping.free-threshold}") BigDecimal threshold) {
    if (fee.signum() < 0 || threshold.signum() < 0)
      throw new IllegalArgumentException("Shipping settings must be non-negative");
    shippingFee = fee;
    freeThreshold = threshold;
  }

  public BigDecimal fee(BigDecimal subtotal) {
    return subtotal.signum() == 0 || subtotal.compareTo(freeThreshold) >= 0
        ? BigDecimal.ZERO
        : shippingFee;
  }

  public StorefrontSettingsResponse settings() {
    return new StorefrontSettingsResponse(shippingFee, freeThreshold);
  }
}
