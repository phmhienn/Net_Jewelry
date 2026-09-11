package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.SanPham;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SanPhamRepository extends BaseRepository<SanPham> {
  @Query("select distinct p.material from SanPham p where p.status=:status order by p.material")
  List<String> materials(
      @org.springframework.data.repository.query.Param("status") ProductStatus status);

  boolean existsBySku(String sku);

  boolean existsByCategoryId(Long id);

  boolean existsByBrandId(Long id);
}
