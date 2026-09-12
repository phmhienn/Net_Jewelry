package com.example.jewelrystore.service;

public interface SePaySignatureService {
  void verify(String rawBody, String signature, String timestamp, String authorization);
}
