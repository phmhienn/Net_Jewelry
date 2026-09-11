package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import java.math.BigDecimal;
import java.util.*;

public interface ProductService {
  List<String> materials();

  PageResponse<ProductResponse> products(
      String keyword,
      Long category,
      Long brand,
      String material,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String sort,
      int page,
      int size,
      boolean admin);

  ProductResponse product(Long id, boolean admin);

  ProductResponse saveProduct(Long id, ProductRequest request);

  void deleteProduct(Long id);

  List<ImageResponse> images(Long id);

  ImageResponse addImage(Long id, ImageRequest request);

  void deleteImage(Long id);

  ImageResponse primaryImage(Long id);

  List<VariantResponse> variants(Long id);

  VariantResponse saveVariant(Long productId, Long id, VariantRequest request);

  void deleteVariant(Long id);
}
