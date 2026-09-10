package com.example.jewelrystore.mapper;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryMapper {

  public DeliveryResponse delivery(GiaoHang gh) {
    return new DeliveryResponse(
        gh.getId(),
        gh.getOrder().getId(),
        gh.getCarrier(),
        gh.getTrackingCode(),
        gh.getShipping(),
        gh.getExpectedAt(),
        gh.getShippedAt(),
        gh.getDeliveredAt(),
        gh.getStatus(),
        gh.getNote());
  }
}
