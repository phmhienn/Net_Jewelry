package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.DonHang;
import com.example.jewelrystore.entity.enums.DomainEnums.OrderStatus;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerQueries extends BaseRepository<DonHang> {
  long countByCustomerId(Long id);

  @Query(
      "select coalesce(sum(o.total),0) from DonHang o where o.customer.id=:id and o.status=:status")
  BigDecimal spending(@Param("id") Long id, @Param("status") OrderStatus status);
}
