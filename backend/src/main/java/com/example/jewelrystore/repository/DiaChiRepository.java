package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.DiaChi;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface DiaChiRepository extends BaseRepository<DiaChi> {
  List<DiaChi> findByCustomerIdOrderById(Long id);
}
