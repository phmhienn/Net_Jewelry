package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.VaiTro;
import java.util.Optional;

public interface VaiTroRepository extends BaseRepository<VaiTro> {
  Optional<VaiTro> findByName(String name);
}
