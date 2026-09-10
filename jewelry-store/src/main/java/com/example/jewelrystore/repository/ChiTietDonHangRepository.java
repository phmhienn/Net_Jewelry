package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.ChiTietDonHang;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ChiTietDonHangRepository extends BaseRepository<ChiTietDonHang> {
  Optional<ChiTietDonHang> findFirstByVariantProductIdAndOrderCustomerIdAndOrderStatusOrderByIdDesc(
      Long productId, Long customerId, OrderStatus status);

  List<ChiTietDonHang> findByOrderIdOrderByVariantId(Long id);

  boolean existsByVariantId(Long id);

  boolean existsByVariantProductIdAndOrderCustomerIdAndOrderStatus(
      Long productId, Long customerId, OrderStatus status);
}
