package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;

public interface WishlistService {
  PageResponse<ProductResponse> list(int page, int size);

  boolean contains(Long productId);

  void add(Long productId);

  void remove(Long productId);
}
