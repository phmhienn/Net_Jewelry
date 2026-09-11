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
public class PaymentMapper {

  public PaymentResponse payment(ThanhToan tt) {
    return new PaymentResponse(
        tt.getId(),
        tt.getOrder().getId(),
        tt.getAmount(),
        tt.getMethod(),
        tt.getStatus(),
        tt.getPaidAt());
  }

  public TransactionResponse transaction(GiaoDich gd) {
    return new TransactionResponse(
        gd.getId(),
        gd.getPayment().getId(),
        gd.getPayment().getOrder().getId(),
        gd.getCode(),
        gd.getAmount(),
        gd.getMethod(),
        gd.getTime(),
        gd.getStatus());
  }
}
