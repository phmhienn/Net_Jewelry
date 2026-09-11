package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.GiaoHang;

public interface GiaoHangRepository extends BaseRepository<GiaoHang> {
  java.util.Optional<GiaoHang> findByOrderId(Long id);
}
