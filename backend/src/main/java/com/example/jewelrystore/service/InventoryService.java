package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.TonKho;
import com.example.jewelrystore.entity.enums.DomainEnums.StockAction;

public interface InventoryService {
  InventoryResponse threshold(
      Long variantId, com.example.jewelrystore.dto.request.StockThresholdRequest request);

  PageResponse<InventoryResponse> list(boolean lowStock, String keyword, int page, int size);

  InventoryResponse get(Long variantId);

  PageResponse<StockHistoryResponse> history(Long variantId, int page, int size);

  InventoryResponse change(InventoryRequest request, StockAction action);

  TonKho lockStock(Long variantId);

  void apply(
      TonKho stock,
      int quantityDelta,
      int reservedDelta,
      StockAction action,
      String reason,
      Long staffId);
}
