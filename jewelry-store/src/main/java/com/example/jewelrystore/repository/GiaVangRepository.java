package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.GiaVang;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface GiaVangRepository extends BaseRepository<GiaVang> {
  boolean existsByTypeAndDate(String type, java.time.LocalDate date);
}
