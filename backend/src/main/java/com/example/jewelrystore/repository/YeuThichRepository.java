package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface YeuThichRepository
    extends JpaRepository<YeuThich, YeuThichId>, JpaSpecificationExecutor<YeuThich> {
  @Modifying
  @Query("delete from YeuThich y where y.product.id=:productId")
  void deleteByProductId(@Param("productId") Long productId);
}
