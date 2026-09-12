package com.example.jewelrystore.controller;

import com.example.jewelrystore.dto.request.SePayWebhookRequest;
import com.example.jewelrystore.dto.response.ApiResponse;
import com.example.jewelrystore.service.SePayService;
import com.example.jewelrystore.service.SePaySignatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment/sepay")
public class SePayController {
  private final SePaySignatureService signatureService;
  private final SePayService sePayService;
  private final ObjectMapper json;

  @PostMapping("/webhook")
  public ResponseEntity<ApiResponse<Void>> webhook(
      @RequestBody String rawBody,
      @RequestHeader(name = "X-SePay-Signature", required = false) String signature,
      @RequestHeader(name = "X-SePay-Timestamp", required = false) String timestamp,
      @RequestHeader(name = "Authorization", required = false) String authorization) {
    signatureService.verify(rawBody, signature, timestamp, authorization);
    SePayWebhookRequest request = json.readValue(rawBody, SePayWebhookRequest.class);
    sePayService.processWebhook(request);
    return ResponseEntity.ok(ApiResponse.done());
  }
}
