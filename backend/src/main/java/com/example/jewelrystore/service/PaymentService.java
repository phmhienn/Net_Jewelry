package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;

public interface PaymentService {
  PaymentResponse get(Long id);

  PaymentResponse confirm(Long id);

  PageResponse<PaymentResponse> list(int page, int size);

  PageResponse<TransactionResponse> transactions(Long paymentId, int page, int size);

  PaymentStatusResponse statusByOrder(Long orderId);

  void expireBankTransferIfNeeded(DonHang order, ThanhToan payment);

  void completeCod(DonHang order);
}
