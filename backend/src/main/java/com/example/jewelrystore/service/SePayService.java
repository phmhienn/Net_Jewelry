package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.SePayWebhookRequest;

public interface SePayService {
  void processWebhook(SePayWebhookRequest request);
}
