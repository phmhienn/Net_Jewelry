package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.NoiDungTrang;
import java.util.Optional;

public interface NoiDungTrangRepository extends BaseRepository<NoiDungTrang> {
  Optional<NoiDungTrang> findBySlug(String slug);
}
