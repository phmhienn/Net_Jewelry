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
public class CartMapper {
  private final HinhAnhSanPhamRepository images;
  private final TonKhoRepository stocks;
  private final ChiTietGioHangRepository cartItems;

  public CartItemResponse cartItem(ChiTietGioHang ctgh) {
    BienTheSanPham btsp = ctgh.getVariant();
    SanPham sp = btsp.getProduct();
    var photos = images.findByProductIdOrderBySortOrderAscIdAsc(sp.getId());
    String image =
        photos.stream()
            .filter(HinhAnhSanPham::isPrimaryImage)
            .findFirst()
            .or(() -> photos.stream().findFirst())
            .map(HinhAnhSanPham::getUrl)
            .orElse(null);
    int available =
        stocks
            .findByVariantId(btsp.getId())
            .map(tk -> tk.getQuantity() - tk.getReserved())
            .orElse(0);
    return new CartItemResponse(
        ctgh.getId(),
        btsp.getId(),
        sp.getId(),
        sp.getName(),
        btsp.getSku(),
        btsp.getSize(),
        btsp.getColor(),
        image,
        sp.getMaterial(),
        ctgh.getQuantity(),
        ctgh.getUnitPrice(),
        ctgh.getTotal(),
        available,
        ctgh.isSelected());
  }

  public CartResponse cart(GioHang gh) {
    return new CartResponse(
        gh.getId(),
        cartItems.findByCartIdOrderByVariantId(gh.getId()).stream().map(this::cartItem).toList(),
        gh.getTotal());
  }
}
