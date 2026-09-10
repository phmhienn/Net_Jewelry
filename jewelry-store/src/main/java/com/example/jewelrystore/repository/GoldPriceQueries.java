package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.GiaVang;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface GoldPriceQueries extends BaseRepository<GiaVang> {
  @Query(
      "select g from GiaVang g where g.date = (select max(g2.date) from GiaVang g2 where g2.type=g.type and g2.date<=current_date) order by g.type")
  List<GiaVang> current();
}
