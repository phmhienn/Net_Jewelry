package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.CartService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
  private final TaiKhoanRepository customers;
  private final GioHangRepository carts;
  private final ChiTietGioHangRepository items;
  private final BienTheSanPhamRepository variants;
  private final TonKhoRepository stocks;
  private final CartMapper cartMapper;
  private final CurrentActor actor;

  private GioHang cart() {
    var customer = lock(customers, actor.customerId());
    return carts
        .findByCustomerId(customer.getId())
        .orElseGet(
            () -> {
              GioHang gh = new GioHang();
              gh.setCustomer(customer);
              return carts.save(gh);
            });
  }

  private void quantity(BienTheSanPham variant, int value) {
    require(value >= 1 && value <= 1000, "Số lượng từ 1 đến 1000");
    sellable(variant);
    var stock =
        stocks
            .findByVariantId(variant.getId())
            .orElseThrow(() -> new InsufficientStockException("Sản phẩm chưa có tồn kho"));
    if (value > stock.getQuantity() - stock.getReserved())
      throw new InsufficientStockException("Số lượng vượt quá tồn kho khả dụng");
  }

  private CartResponse refresh(GioHang cart) {
    BigDecimal sum = BigDecimal.ZERO;
    for (var ctgh : items.findByCartIdOrderByVariantId(cart.getId())) {
      ctgh.setUnitPrice(ctgh.getVariant().getEffectivePrice());
      ctgh.setTotal(ctgh.getUnitPrice().multiply(BigDecimal.valueOf(ctgh.getQuantity())));
      sum = sum.add(ctgh.getTotal());
    }
    cart.setTotal(sum);
    cart.setUpdatedAt(Instant.now());
    return cartMapper.cart(cart);
  }

  public CartResponse get() {
    return refresh(cart());
  }

  public CartResponse add(CartItemRequest request) {
    var cart = cart();
    var all = items.findByCartIdOrderByVariantId(cart.getId());
    var variant = com.example.jewelrystore.util.Checks.get(variants, request.variantId());
    ChiTietGioHang ctgh =
        all.stream()
            .filter(cartItem -> cartItem.getVariant().getId().equals(request.variantId()))
            .findFirst()
            .orElseGet(ChiTietGioHang::new);
    int count = (ctgh.getId() == null ? 0 : ctgh.getQuantity()) + request.quantity();
    quantity(variant, count);
    require(ctgh.getId() != null || all.size() < 100, "Giỏ hàng tối đa 100 dòng");
    ctgh.setSelected(true);
    ctgh.setCart(cart);
    ctgh.setVariant(variant);
    ctgh.setQuantity(count);
    ctgh.setUnitPrice(variant.getEffectivePrice());
    ctgh.setTotal(ctgh.getUnitPrice().multiply(BigDecimal.valueOf(count)));
    items.save(ctgh);
    return refresh(cart);
  }

  public CartResponse update(Long id, QuantityRequest request) {
    var cart = cart();
    ChiTietGioHang ctgh = com.example.jewelrystore.util.Checks.get(items, id);
    owner(ctgh.getCart().getId(), cart.getId());
    quantity(ctgh.getVariant(), request.quantity());
    ctgh.setQuantity(request.quantity());
    ctgh.setUnitPrice(ctgh.getVariant().getEffectivePrice());
    ctgh.setTotal(ctgh.getUnitPrice().multiply(BigDecimal.valueOf(ctgh.getQuantity())));
    return refresh(cart);
  }

  public CartResponse remove(Long id) {
    var cart = cart();
    ChiTietGioHang ctgh = com.example.jewelrystore.util.Checks.get(items, id);
    owner(ctgh.getCart().getId(), cart.getId());
    items.delete(ctgh);
    items.flush();
    return refresh(cart);
  }

  public CartResponse select(Long id, SelectionRequest request) {
    var gh = cart();
    var ctgh = com.example.jewelrystore.util.Checks.get(items, id);
    owner(ctgh.getCart().getId(), gh.getId());
    ctgh.setSelected(request.selected());
    return refresh(gh);
  }

  public CartResponse clear() {
    var cart = cart();
    items.deleteByCartId(cart.getId());
    items.flush();
    return refresh(cart);
  }
}
