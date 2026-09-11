package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import java.time.LocalDate;
import java.util.List;

public interface GoldPriceService {
  List<GoldPriceResponse> current();

  PageResponse<GoldPriceResponse> history(
      String type, LocalDate from, LocalDate to, int page, int size);

  GoldPriceResponse save(Long id, GoldPriceRequest request);

  void delete(Long id);
}
