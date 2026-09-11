package com.example.jewelrystore.mapper;

import com.example.jewelrystore.config.SePayConfig;
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
  private final SePayConfig sePayConfig;

  public PaymentResponse payment(ThanhToan tt) {
    var instruction =
        tt.getMethod() == PaymentMethod.BANK_TRANSFER
            ? new PaymentInstructionResponse(
                sePayConfig.bankCode(),
                sePayConfig.accountNumber(),
                sePayConfig.accountName(),
                tt.getAmount(),
                tt.getOrder().getCode(),
                sePayConfig.vietQrUrl(tt.getAmount(), tt.getOrder().getCode()))
            : null;
    return new PaymentResponse(
        tt.getId(),
        tt.getOrder().getId(),
        tt.getAmount(),
        tt.getMethod(),
        tt.getStatus(),
        tt.getPaidAt(),
        instruction);
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
