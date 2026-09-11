package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.GiaoDich;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface GiaoDichRepository extends BaseRepository<GiaoDich> {
  Page<GiaoDich> findByPaymentId(Long id, Pageable pageable);

  boolean existsByCode(String code);
}
