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
public class InventoryMapper {

  public InventoryResponse stock(TonKho tk) {
    return new InventoryResponse(
        tk.getVariant().getId(),
        tk.getVariant().getSku(),
        tk.getVariant().getProduct().getName(),
        tk.getQuantity(),
        tk.getReserved(),
        tk.getQuantity() - tk.getReserved(),
        tk.getQuantity() - tk.getReserved() <= tk.getLowStockThreshold(),
        tk.getUpdatedAt(),
        tk.getLowStockThreshold());
  }

  public StockHistoryResponse history(LichSuKho lsk) {
    return new StockHistoryResponse(
        lsk.getId(),
        lsk.getVariant().getId(),
        lsk.getAccount() == null ? null : lsk.getAccount().getId(),
        lsk.getAction(),
        lsk.getQuantityChange(),
        lsk.getReservedChange(),
        lsk.getQuantityAfter(),
        lsk.getReservedAfter(),
        lsk.getReason(),
        lsk.getCreatedAt());
  }
}
