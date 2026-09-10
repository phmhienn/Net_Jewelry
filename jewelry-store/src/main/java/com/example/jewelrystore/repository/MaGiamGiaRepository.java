package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.MaGiamGia;

public interface MaGiamGiaRepository extends BaseRepository<MaGiamGia> {
  java.util.Optional<MaGiamGia> findByCode(String code);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select mgg from MaGiamGia mgg where mgg.code=:code")
  java.util.Optional<MaGiamGia> lockByCode(
      @org.springframework.data.repository.query.Param("code") String code);
}
