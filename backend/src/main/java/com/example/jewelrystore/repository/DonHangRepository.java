package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.DonHang;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface DonHangRepository extends BaseRepository<DonHang> {
  Optional<DonHang> findByCustomerIdAndIdempotencyKey(Long customerId, String key);

  Page<DonHang> findByCustomerId(Long customerId, Pageable pageable);
}
