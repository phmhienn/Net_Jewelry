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
public class CategoryMapper {

  public CategoryResponse category(DanhMuc dm) {
    return new CategoryResponse(
        dm.getId(),
        dm.getName(),
        dm.getParent() == null ? null : dm.getParent().getId(),
        dm.getDescription(),
        dm.getStatus(),
        List.of());
  }
}
