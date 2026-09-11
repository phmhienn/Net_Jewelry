package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.DeliveryRequest;
import com.example.jewelrystore.dto.response.DeliveryResponse;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.*;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {
  private final GiaoHangRepository deliveries;
  private final DonHangRepository orders;
  private final DeliveryMapper deliveryMapper;

  public void create(DonHang dh) {
    var gh = new GiaoHang();
    gh.setOrder(dh);
    gh.setShipping(dh.getShipping());
    deliveries.save(gh);
  }

  public DeliveryResponse update(Long orderId, DeliveryRequest request) {
    var dh = lock(orders, orderId);
    require(
        dh.getStatus() != OrderStatus.HOAN_THANH && dh.getStatus() != OrderStatus.DA_HUY,
        "Đơn hàng đã kết thúc");
    var gh = deliveries.findByOrderId(orderId).orElseThrow();
    gh.setCarrier(request.carrier());
    gh.setTrackingCode(request.trackingCode());
    gh.setExpectedAt(request.expectedAt());
    gh.setNote(request.note());
    return deliveryMapper.delivery(gh);
  }

  public void transition(DonHang dh) {
    var gh =
        deliveries
            .findByOrderId(dh.getId())
            .orElseGet(
                () -> {
                  create(dh);
                  return deliveries.findByOrderId(dh.getId()).orElseThrow();
                });
    if (dh.getStatus() == OrderStatus.DANG_GIAO_HANG) {
      gh.setStatus(DeliveryStatus.DANG_GIAO);
      gh.setShippedAt(Instant.now());
    }
    if (dh.getStatus() == OrderStatus.HOAN_THANH) {
      gh.setStatus(DeliveryStatus.DA_GIAO);
      gh.setDeliveredAt(Instant.now());
    }
  }

  public void cancel(DonHang dh) {
    deliveries
        .findByOrderId(dh.getId())
        .ifPresent(
            gh -> {
              gh.setStatus(DeliveryStatus.THAT_BAI);
              gh.setNote("Đơn hàng đã hủy trước khi giao");
            });
  }
}
