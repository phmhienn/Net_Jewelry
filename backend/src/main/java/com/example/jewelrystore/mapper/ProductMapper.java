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
public class ProductMapper {
  private final HinhAnhSanPhamRepository images;
  private final BienTheSanPhamRepository variants;
  private final TonKhoRepository stocks;
  private final DanhGiaRepository reviews;

  public ImageResponse image(HinhAnhSanPham hasp) {
    return new ImageResponse(
        hasp.getId(), hasp.getUrl(), hasp.isPrimaryImage(), hasp.getSortOrder());
  }

  public VariantResponse variant(BienTheSanPham btsp) {
    var stockOptional = stocks.findByVariantId(btsp.getId());
    return new VariantResponse(
        btsp.getId(),
        btsp.getSku(),
        btsp.getSize(),
        btsp.getColor(),
        btsp.getPrice(),
        stockOptional.map(TonKho::getQuantity).orElse(0),
        stockOptional.map(TonKho::getReserved).orElse(0),
        stockOptional.map(tk -> tk.getQuantity() - tk.getReserved()).orElse(0),
        btsp.getSalePrice(),
        btsp.getStatus());
  }

  public ProductResponse product(SanPham sp) {
    Double rating = reviews.average(sp.getId(), ReviewStatus.HIEN_THI);
    return new ProductResponse(
        sp.getId(),
        sp.getSku(),
        sp.getName(),
        sp.getCategory().getId(),
        sp.getCategory().getName(),
        sp.getBrand().getId(),
        sp.getBrand().getName(),
        sp.getDescription(),
        sp.getPrice(),
        sp.getSize(),
        sp.getColor(),
        sp.getMaterial(),
        sp.getWeight(),
        sp.getGemstone(),
        sp.getStatus(),
        sp.getCreatedAt(),
        images.findByProductIdOrderBySortOrderAscIdAsc(sp.getId()).stream()
            .map(this::image)
            .toList(),
        variants.findByProductIdOrderById(sp.getId()).stream().map(this::variant).toList(),
        rating == null ? 0 : rating,
        reviews.countByProductIdAndStatus(sp.getId(), ReviewStatus.HIEN_THI),
        sp.getSalePrice());
  }
}
