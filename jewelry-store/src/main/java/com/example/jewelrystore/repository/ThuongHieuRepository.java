package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.ThuongHieu;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ThuongHieuRepository extends BaseRepository<ThuongHieu> {
  boolean existsByName(String name);
}
