package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.BienTheSanPham;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface BienTheSanPhamRepository extends BaseRepository<BienTheSanPham> {
  List<BienTheSanPham> findByProductIdOrderById(Long id);

  boolean existsBySku(String sku);
}
