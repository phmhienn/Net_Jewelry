package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.CategoryService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
  private final SanPhamRepository products;
  private final DanhMucRepository categories;
  private final CategoryMapper categoryMapper;

  @Transactional(readOnly = true)
  public List<CategoryResponse> categories(boolean tree) {
    var all = categories.findAll(Sort.by("id"));
    return tree
        ? tree(all, null, new HashSet<>())
        : all.stream().map(categoryMapper::category).toList();
  }

  private List<CategoryResponse> tree(List<DanhMuc> all, Long parent, Set<Long> path) {
    return all.stream()
        .filter(
            dm -> Objects.equals(dm.getParent() == null ? null : dm.getParent().getId(), parent))
        .map(
            dm -> {
              require(!path.contains(dm.getId()), "Danh mục bị vòng lặp");
              var next = new HashSet<>(path);
              next.add(dm.getId());
              return new CategoryResponse(
                  dm.getId(),
                  dm.getName(),
                  parent,
                  dm.getDescription(),
                  dm.getStatus(),
                  tree(all, dm.getId(), next));
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse category(Long id) {
    return categoryMapper.category(get(categories, id));
  }

  public CategoryResponse saveCategory(Long id, CategoryRequest request) {
    categories.lockTree();
    DanhMuc dm = id == null ? new DanhMuc() : lock(categories, id);
    if (id == null || !dm.getName().equals(request.name()))
      require(!categories.existsByName(request.name()), "Tên danh mục đã tồn tại");
    DanhMuc parent = request.parentId() == null ? null : get(categories, request.parentId());
    var current = parent;
    Set<Long> seen = new HashSet<>();
    while (current != null) {
      require(
          !current.getId().equals(id) && seen.add(current.getId()), "Danh mục cha tạo vòng lặp");
      current = current.getParent();
    }
    dm.setName(request.name());
    dm.setParent(parent);
    dm.setDescription(request.description());
    dm.setStatus(request.status());
    return categoryMapper.category(categories.save(dm));
  }

  public void deleteCategory(Long id) {
    DanhMuc dm = get(categories, id);
    require(
        !categories.existsByParentId(id) && !products.existsByCategoryId(id),
        "Danh mục đang được sử dụng");
    categories.delete(dm);
  }
}
