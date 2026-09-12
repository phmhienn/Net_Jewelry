package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.LichSuKho;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface LichSuKhoRepository extends BaseRepository<LichSuKho> {
  Page<LichSuKho> findByVariantId(Long id, Pageable pageable);

  void deleteByVariantId(Long id);
}
