package com.example.jewelrystore.mapper;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OrderMapper {
  private final ChiTietDonHangRepository orderItems;
  private final ThanhToanRepository payments;
  private final ObjectMapper json;
  private final GiaoHangRepository deliveries;
  private final PaymentMapper paymentMapper;
  private final DeliveryMapper deliveryMapper;

  public OrderLineResponse orderLine(ChiTietDonHang ctdh) {
    return new OrderLineResponse(
        ctdh.getId(),
        ctdh.getVariant().getId(),
        ctdh.getVariant().getProduct().getId(),
        ctdh.getProductName(),
        ctdh.getSku(),
        ctdh.getQuantity(),
        ctdh.getUnitPrice(),
        ctdh.getTotal(),
        ctdh.getSize(),
        ctdh.getColor());
  }

  public OrderResponse order(DonHang dh) {
    return new OrderResponse(
        dh.getId(),
        dh.getCustomer().getId(),
        dh.getDate(),
        dh.getSubtotal(),
        dh.getShipping(),
        dh.getTotal(),
        dh.getNote(),
        orderAddress(dh),
        dh.getShippingMethod(),
        dh.getStatus(),
        orderItems.findByOrderIdOrderByVariantId(dh.getId()).stream().map(this::orderLine).toList(),
        payments.findByOrderId(dh.getId()).map(paymentMapper::payment).orElse(null),
        dh.getCode(),
        dh.getDiscount(),
        dh.getCoupon() == null ? null : dh.getCoupon().getCode(),
        deliveries.findByOrderId(dh.getId()).map(deliveryMapper::delivery).orElse(null));
  }

  private AddressResponse orderAddress(DonHang dh) {
    // Existing SQL data may contain a plain address; old API orders used JSON.
    try {
      if (dh.getAddressSnapshot().trim().startsWith("{"))
        return json.readValue(dh.getAddressSnapshot(), AddressResponse.class);
    } catch (RuntimeException ignored) {
    }
    return new AddressResponse(
        null,
        dh.getRecipientName(),
        dh.getRecipientPhone(),
        "",
        "",
        "",
        dh.getAddressSnapshot(),
        false);
  }
}
