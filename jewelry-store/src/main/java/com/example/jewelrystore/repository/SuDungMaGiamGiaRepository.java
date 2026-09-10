package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.SuDungMaGiamGia;

public interface SuDungMaGiamGiaRepository extends BaseRepository<SuDungMaGiamGia> {
  java.util.Optional<SuDungMaGiamGia> findByOrderId(Long id);
}
