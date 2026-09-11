package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.*;
import com.example.jewelrystore.util.Pages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {
  private final YeuThichRepository wishes;
  private final TaiKhoanRepository accounts;
  private final SanPhamRepository products;
  private final ProductMapper productMapper;
  private final CurrentActor actor;

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> list(int page, int size) {
    Long id = actor.customerId();
    return PageResponse.of(
        wishes
            .findAll(
                (root, query, cb) ->
                    cb.and(
                        cb.equal(root.get("customer").get("id"), id),
                        cb.equal(root.get("product").get("status"), ProductStatus.DANG_BAN),
                        cb.equal(
                            root.get("product").get("category").get("status"),
                            CategoryStatus.HOAT_DONG),
                        cb.equal(
                            root.get("product").get("brand").get("status"),
                            CategoryStatus.HOAT_DONG)),
                Pages.of(
                    page,
                    size,
                    org.springframework.data.domain.Sort.by("addedAt")
                        .descending()
                        .and(org.springframework.data.domain.Sort.by("product.id"))))
            .map(yt -> productMapper.product(yt.getProduct())));
  }

  @Transactional(readOnly = true)
  public boolean contains(Long productId) {
    return wishes.existsById(new YeuThichId(actor.customerId(), productId));
  }

  public void add(Long productId) {
    var tk = lock(accounts, actor.customerId());
    var sp = get(products, productId);
    require(
        sp.getStatus() == ProductStatus.DANG_BAN
            && sp.getCategory().getStatus() == CategoryStatus.HOAT_DONG
            && sp.getBrand().getStatus() == CategoryStatus.HOAT_DONG,
        "Sản phẩm đã ngừng bán");
    var id = new YeuThichId(tk.getId(), productId);
    if (wishes.existsById(id)) return;
    var yt = new YeuThich();
    yt.setCustomer(tk);
    yt.setProduct(sp);
    wishes.save(yt);
  }

  public void remove(Long productId) {
    lock(accounts, actor.customerId());
    wishes.deleteById(new YeuThichId(actor.customerId(), productId));
  }
}
