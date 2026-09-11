package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.DanhMuc;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface DanhMucRepository extends BaseRepository<DanhMuc> {
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from DanhMuc c order by c.id")
  List<DanhMuc> lockTree();

  boolean existsByName(String name);

  boolean existsByParentId(Long id);

  List<DanhMuc> findByStatusOrderById(CategoryStatus status);
}
