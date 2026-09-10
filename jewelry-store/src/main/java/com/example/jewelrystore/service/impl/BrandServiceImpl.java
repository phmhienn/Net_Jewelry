package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.BrandService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {
  private final SanPhamRepository products;
  private final ThuongHieuRepository brands;
  private final BrandMapper brandMapper;

  @Transactional(readOnly = true)
  public List<BrandResponse> brands() {
    return brands.findAll(Sort.by("name")).stream().map(brandMapper::brand).toList();
  }

  @Transactional(readOnly = true)
  public BrandResponse brand(Long id) {
    return brandMapper.brand(get(brands, id));
  }

  public BrandResponse saveBrand(Long id, BrandRequest request) {
    ThuongHieu th = id == null ? new ThuongHieu() : lock(brands, id);
    if (id == null || !th.getName().equals(request.name()))
      require(!brands.existsByName(request.name()), "Tên thương hiệu đã tồn tại");
    th.setName(request.name());
    th.setLogo(imageUrl(request.logo()));
    th.setDescription(request.description());
    th.setStatus(request.status() == null ? CategoryStatus.HOAT_DONG : request.status());
    return brandMapper.brand(brands.save(th));
  }

  public void deleteBrand(Long id) {
    ThuongHieu th = get(brands, id);
    require(!products.existsByBrandId(id), "Thương hiệu đang được sử dụng");
    brands.delete(th);
  }
}
