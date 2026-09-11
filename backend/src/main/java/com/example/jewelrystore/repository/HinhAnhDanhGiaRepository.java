package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.HinhAnhDanhGia;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface HinhAnhDanhGiaRepository extends BaseRepository<HinhAnhDanhGia> {
  List<HinhAnhDanhGia> findByReviewId(Long id);

  void deleteByReviewId(Long id);
}
