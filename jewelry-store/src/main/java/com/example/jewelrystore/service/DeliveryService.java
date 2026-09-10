package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.DeliveryRequest;
import com.example.jewelrystore.dto.response.DeliveryResponse;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;

public interface DeliveryService {
  void create(DonHang dh);

  DeliveryResponse update(Long orderId, DeliveryRequest request);

  void transition(DonHang dh);

  void cancel(DonHang dh);
}
