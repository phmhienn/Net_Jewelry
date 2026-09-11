package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.GioHang;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface GioHangRepository extends BaseRepository<GioHang> {
  Optional<GioHang> findByCustomerId(Long id);
}
