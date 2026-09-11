package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.InventoryService;
import com.example.jewelrystore.util.Pages;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
  private final TonKhoRepository stocks;
  private final LichSuKhoRepository history;
  private final TaiKhoanRepository staff;
  private final InventoryMapper inventoryMapper;
  private final CurrentActor actor;

  @Transactional(readOnly = true)
  public PageResponse<InventoryResponse> list(boolean lowStock, String keyword, int page, int size) {
    final var search =
        keyword == null || keyword.isBlank() ? null : "%" + keyword.trim().toLowerCase() + "%";
    return PageResponse.of(
        stocks
            .findAll(
                (root, criteriaQuery, cb) -> {
                  var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                  if (lowStock)
                    predicates.add(
                        cb.le(
                            cb.diff(root.get("quantity"), root.get("reserved")),
                            root.get("lowStockThreshold")));
                  if (search != null) {
                    var variant = root.join("variant");
                    var product = variant.join("product");
                    predicates.add(
                        cb.or(
                            cb.like(cb.lower(variant.get("sku")), search),
                            cb.like(cb.lower(product.get("sku")), search),
                            cb.like(cb.lower(product.get("name")), search)));
                  }
                  return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                },
                Pages.of(page, size))
            .map(inventoryMapper::stock));
  }

  @Transactional(readOnly = true)
  public InventoryResponse get(Long variantId) {
    return inventoryMapper.stock(
        stocks
            .findByVariantId(variantId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho")));
  }

  @Transactional(readOnly = true)
  public PageResponse<StockHistoryResponse> history(Long variantId, int page, int size) {
    return PageResponse.of(
        history.findByVariantId(variantId, Pages.of(page, size)).map(inventoryMapper::history));
  }

  public TonKho lockStock(Long variantId) {
    return stocks
        .lockVariant(variantId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho"));
  }

  public InventoryResponse threshold(Long variantId, StockThresholdRequest request) {
    var tk = lockStock(variantId);
    tk.setLowStockThreshold(request.threshold());
    return inventoryMapper.stock(tk);
  }

  public InventoryResponse change(InventoryRequest request, StockAction action) {
    var stock = lockStock(request.variantId());
    require(action == StockAction.DIEU_CHINH || request.quantity() > 0, "Số lượng phải lớn hơn 0");
    int delta =
        switch (action) {
          case NHAP -> request.quantity();
          case XUAT -> -request.quantity();
          case DIEU_CHINH -> request.quantity() - stock.getQuantity();
          default -> throw new BadRequestException("Thao tác kho không hợp lệ");
        };
    apply(stock, delta, 0, action, request.reason(), actor.get().id());
    return inventoryMapper.stock(stock);
  }

  public void apply(
      TonKho stock,
      int quantityDelta,
      int reservedDelta,
      StockAction action,
      String reason,
      Long staffId) {
    long quantity = (long) stock.getQuantity() + quantityDelta;
    long reserved = (long) stock.getReserved() + reservedDelta;
    if (quantity < 0 || reserved < 0 || quantity < reserved || quantity > Integer.MAX_VALUE)
      throw new InsufficientStockException("Không đủ tồn kho cho " + stock.getVariant().getSku());
    stock.setQuantity((int) quantity);
    stock.setReserved((int) reserved);
    stock.setUpdatedAt(Instant.now());
    LichSuKho lsk = new LichSuKho();
    lsk.setVariant(stock.getVariant());
    lsk.setAccount(
        staffId == null ? null : com.example.jewelrystore.util.Checks.get(staff, staffId));
    lsk.setAction(action);
    lsk.setQuantityChange(quantityDelta);
    lsk.setReservedChange(reservedDelta);
    lsk.setQuantityAfter((int) quantity);
    lsk.setReservedAfter((int) reserved);
    lsk.setReason(reason);
    history.save(lsk);
  }
}
