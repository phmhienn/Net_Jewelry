package com.example.jewelrystore.exception;

public class InsufficientStockException extends ApiException {
  public InsufficientStockException(String message) {
    super(409, message);
  }
}
