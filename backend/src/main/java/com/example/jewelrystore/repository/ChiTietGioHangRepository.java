package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.ChiTietGioHang;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ChiTietGioHangRepository extends BaseRepository<ChiTietGioHang> {
  List<ChiTietGioHang> findByCartIdOrderByVariantId(Long id);

  boolean existsByVariantId(Long id);

  void deleteByVariantId(Long id);

  void deleteByCartId(Long id);
}
