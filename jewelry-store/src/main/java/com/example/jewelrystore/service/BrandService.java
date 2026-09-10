package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import java.util.*;

public interface BrandService {
  List<BrandResponse> brands();

  BrandResponse brand(Long id);

  BrandResponse saveBrand(Long id, BrandRequest request);

  void deleteBrand(Long id);
}
