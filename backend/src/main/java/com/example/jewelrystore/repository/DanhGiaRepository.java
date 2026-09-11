package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.DanhGia;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DanhGiaRepository extends BaseRepository<DanhGia> {
  boolean existsByProductIdAndCustomerId(Long productId, Long customerId);

  Page<DanhGia> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable page);

  @Query("select avg(d.stars) from DanhGia d where d.product.id=:id and d.status=:status")
  Double average(@Param("id") Long id, @Param("status") ReviewStatus status);

  long countByProductIdAndStatus(Long id, ReviewStatus status);
}
