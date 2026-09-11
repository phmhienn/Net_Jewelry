package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.HinhAnhSanPham;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface HinhAnhSanPhamRepository extends BaseRepository<HinhAnhSanPham> {
  List<HinhAnhSanPham> findByProductIdOrderBySortOrderAscIdAsc(Long id);
}
