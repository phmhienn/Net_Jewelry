package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.TonKho;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TonKhoRepository extends BaseRepository<TonKho> {
  Optional<TonKho> findByVariantId(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from TonKho t where t.variant.id=:id")
  Optional<TonKho> lockVariant(@Param("id") Long id);
}
