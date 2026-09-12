package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.ProductService;
import com.example.jewelrystore.util.Pages;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
  private final SanPhamRepository products;
  private final DanhMucRepository categories;
  private final ThuongHieuRepository brands;
  private final BienTheSanPhamRepository variants;
  private final HinhAnhSanPhamRepository images;
  private final TonKhoRepository stocks;
  private final ChiTietGioHangRepository cartItems;
  private final ChiTietDonHangRepository orderItems;
  private final LichSuKhoRepository history;
  private final ProductMapper productMapper;

  @Transactional(readOnly = true)
  public List<String> materials() {
    return products.materials(ProductStatus.DANG_BAN);
  }

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> products(
      String keyword,
      Long category,
      Long brand,
      String material,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String sort,
      int page,
      int size,
      boolean admin) {
    require(minPrice == null || minPrice.signum() >= 0, "Giá tối thiểu không hợp lệ");
    require(maxPrice == null || maxPrice.signum() >= 0, "Giá tối đa không hợp lệ");
    require(
        minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0,
        "Khoảng giá không hợp lệ");
    Specification<SanPham> spec =
        (root, criteriaQuery, cb) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (!admin) {
            predicates.add(cb.equal(root.get("status"), ProductStatus.DANG_BAN));
            predicates.add(cb.equal(root.get("brand").get("status"), CategoryStatus.HOAT_DONG));
            predicates.add(cb.equal(root.get("category").get("status"), CategoryStatus.HOAT_DONG));
          }
          if (keyword != null && !keyword.isBlank()) {
            String escaped =
                keyword
                    .trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'),
                    cb.like(cb.lower(root.get("sku")), "%" + escaped + "%", '!')));
          }
          if (category != null) predicates.add(cb.equal(root.get("category").get("id"), category));
          if (brand != null) predicates.add(cb.equal(root.get("brand").get("id"), brand));
          if (material != null && !material.isBlank())
            predicates.add(cb.equal(root.get("material"), material));
          if (minPrice != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("displayPrice"), minPrice));
          if (maxPrice != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("displayPrice"), maxPrice));
          return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    Sort order =
        switch (sort) {
          case "priceAsc" -> Sort.by("displayPrice").ascending();
          case "priceDesc" -> Sort.by("displayPrice").descending();
          case "newest" -> Sort.by("createdAt").descending();
          default ->
              throw new com.example.jewelrystore.exception.BadRequestException(
                  "Sắp xếp không hợp lệ");
        };
    return PageResponse.of(
        products
            .findAll(spec, Pages.of(page, size, order.and(Sort.by("id").descending())))
            .map(productMapper::product));
  }

  @Transactional(readOnly = true)
  public ProductResponse product(Long id, boolean admin) {
    SanPham sp = get(products, id);
    if (!admin)
      require(
          sp.getStatus() == ProductStatus.DANG_BAN
              && sp.getBrand().getStatus() == CategoryStatus.HOAT_DONG
              && sp.getCategory().getStatus() == CategoryStatus.HOAT_DONG,
          "Sản phẩm đã ngừng bán");
    return productMapper.product(sp);
  }

  public ProductResponse saveProduct(Long id, ProductRequest request) {
    SanPham sp = id == null ? new SanPham() : lock(products, id);
    if (id == null || !sp.getSku().equals(request.sku()))
      require(!products.existsBySku(request.sku()), "SKU đã tồn tại");
    sp.setSku(request.sku());
    sp.setName(request.name());
    sp.setCategory(get(categories, request.categoryId()));
    sp.setBrand(get(brands, request.brandId()));
    sp.setDescription(request.description());
    require(
        request.salePrice() == null || request.salePrice().compareTo(request.price()) <= 0,
        "Giá khuyến mãi không được vượt giá gốc");
    sp.setSalePrice(request.salePrice());
    sp.setPrice(request.price());
    sp.setSize(request.size());
    sp.setColor(request.color());
    sp.setMaterial(request.material());
    sp.setWeight(request.weight());
    sp.setGemstone(request.gemstone());
    sp.setStatus(request.status());
    sp = products.save(sp);
    ensureDefaultVariantAndStock(sp);
    return productMapper.product(sp);
  }

  public void deleteProduct(Long id) {
    lock(products, id).setStatus(ProductStatus.NGUNG_BAN);
  }

  @Transactional(readOnly = true)
  public List<ImageResponse> images(Long id) {
    product(id, false);
    return images.findByProductIdOrderBySortOrderAscIdAsc(id).stream()
        .map(productMapper::image)
        .toList();
  }

  public ImageResponse addImage(Long id, ImageRequest request) {
    SanPham sp = lock(products, id);
    var all = images.findByProductIdOrderBySortOrderAscIdAsc(id);
    require(all.size() < 12, "Tối đa 12 ảnh mỗi sản phẩm");
    boolean primary = request.primaryImage() || all.isEmpty();
    if (primary) all.forEach(hasp -> hasp.setPrimaryImage(false));
    HinhAnhSanPham hasp = new HinhAnhSanPham();
    hasp.setProduct(sp);
    hasp.setUrl(imageUrl(request.url()));
    hasp.setPrimaryImage(primary);
    hasp.setSortOrder(request.sortOrder() == null ? all.size() : request.sortOrder());
    return productMapper.image(images.save(hasp));
  }

  public void deleteImage(Long id) {
    HinhAnhSanPham hasp = get(images, id);
    lock(products, hasp.getProduct().getId());
    boolean primary = hasp.isPrimaryImage();
    images.delete(hasp);
    images.flush();
    if (primary)
      images.findByProductIdOrderBySortOrderAscIdAsc(hasp.getProduct().getId()).stream()
          .findFirst()
          .ifPresent(anhThayThe -> anhThayThe.setPrimaryImage(true));
  }

  public ImageResponse primaryImage(Long id) {
    var found = get(images, id);
    lock(products, found.getProduct().getId());
    images
        .findByProductIdOrderBySortOrderAscIdAsc(found.getProduct().getId())
        .forEach(hasp -> hasp.setPrimaryImage(hasp.getId().equals(id)));
    return productMapper.image(found);
  }

  @Transactional(readOnly = true)
  public List<VariantResponse> variants(Long id) {
    product(id, false);
    return variants.findByProductIdOrderById(id).stream().map(productMapper::variant).toList();
  }

  public VariantResponse saveVariant(Long productId, Long id, VariantRequest request) {
    BienTheSanPham btsp = id == null ? new BienTheSanPham() : lock(variants, id);
    if (id == null) btsp.setProduct(get(products, productId));
    if (id == null || !btsp.getSku().equals(request.sku()))
      require(!variants.existsBySku(request.sku()), "SKU biến thể đã tồn tại");
    btsp.setSku(request.sku());
    btsp.setSize(request.size());
    btsp.setColor(request.color());
    require(
        request.salePrice() == null || request.salePrice().compareTo(request.price()) <= 0,
        "Giá khuyến mãi không được vượt giá gốc");
    btsp.setSalePrice(request.salePrice());
    btsp.setPrice(request.price());
    btsp.setStatus(request.status() == null ? ProductStatus.DANG_BAN : request.status());
    variants.save(btsp);
    ensureStock(btsp);
    return productMapper.variant(btsp);
  }


  private void ensureDefaultVariantAndStock(SanPham sp) {
    if (!variants.findByProductIdOrderById(sp.getId()).isEmpty()) return;

    BienTheSanPham btsp = new BienTheSanPham();
    btsp.setProduct(sp);
    btsp.setSku(defaultVariantSku(sp));
    btsp.setSize(sp.getSize());
    btsp.setColor(sp.getColor());
    btsp.setPrice(sp.getPrice());
    btsp.setSalePrice(sp.getSalePrice());
    btsp.setStatus(sp.getStatus());
    variants.save(btsp);
    ensureStock(btsp);
  }

  private String defaultVariantSku(SanPham sp) {
    String base = sp.getSku();
    if (!variants.existsBySku(base)) return base;

    String withSuffix = base + "-DEFAULT";
    if (!variants.existsBySku(withSuffix)) return withSuffix;

    return base + "-" + sp.getId();
  }

  private void ensureStock(BienTheSanPham btsp) {
    if (btsp.getId() != null && stocks.findByVariantId(btsp.getId()).isPresent()) return;
    var stock = new TonKho();
    stock.setVariant(btsp);
    stocks.save(stock);
  }

  public void deleteVariant(Long id) {
    BienTheSanPham btsp = lock(variants, id);
    var stock = stocks.lockVariant(id).orElseThrow();
    require(
        !cartItems.existsByVariantId(id) && !orderItems.existsByVariantId(id),
        "Biến thể đã được sử dụng, hãy ngừng bán sản phẩm");
    require(
        stock.getQuantity() == 0
            && stock.getReserved() == 0
            && history.findByVariantId(id, Pages.of(0, 1)).isEmpty(),
        "Biến thể có tồn kho hoặc lịch sử kho");
    stocks.delete(stock);
    variants.delete(btsp);
  }
}
