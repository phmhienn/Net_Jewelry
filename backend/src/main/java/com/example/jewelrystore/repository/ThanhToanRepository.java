package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.ThanhToan;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ThanhToanRepository extends BaseRepository<ThanhToan> {
  @Query("select p.order.id from ThanhToan p where p.id=:id")
  Optional<Long> orderId(@org.springframework.data.repository.query.Param("id") Long id);

  Optional<ThanhToan> findByOrderId(Long id);
}
