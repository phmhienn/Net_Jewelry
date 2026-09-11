package com.example.jewelrystore.mapper;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoldPriceMapper {

  public GoldPriceResponse gold(GiaVang gv) {
    return new GoldPriceResponse(
        gv.getId(),
        gv.getType(),
        gv.getBuyPrice(),
        gv.getSellPrice(),
        gv.getDate(),
        gv.getUnit(),
        gv.getUpdatedAt());
  }
}
