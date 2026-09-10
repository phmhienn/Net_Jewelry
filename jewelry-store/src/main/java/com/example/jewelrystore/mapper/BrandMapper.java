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
public class BrandMapper {

  public BrandResponse brand(ThuongHieu th) {
    return new BrandResponse(
        th.getId(), th.getName(), th.getLogo(), th.getDescription(), th.getStatus());
  }
}
