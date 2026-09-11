package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import java.util.*;

public interface CategoryService {
  List<CategoryResponse> categories(boolean tree);

  CategoryResponse category(Long id);

  CategoryResponse saveCategory(Long id, CategoryRequest request);

  void deleteCategory(Long id);
}
