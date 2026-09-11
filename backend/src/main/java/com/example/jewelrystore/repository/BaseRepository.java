package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.BaseEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity>
    extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from #{#entityName} e where e.id=:id")
  Optional<T> findLockedById(@Param("id") Long id);
}
