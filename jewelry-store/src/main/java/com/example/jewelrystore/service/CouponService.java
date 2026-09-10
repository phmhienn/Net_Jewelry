package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.math.*;

public interface CouponService {
  CouponResponse response(MaGiamGia mgg);

  PageResponse<CouponResponse> list(int page, int size);

  CouponResponse save(Long id, CouponRequest request);

  void disable(Long id);

  CheckoutQuoteResponse quote(String code);

  void apply(DonHang dh, String code);

  void record(DonHang dh);

  void release(DonHang dh);
}
