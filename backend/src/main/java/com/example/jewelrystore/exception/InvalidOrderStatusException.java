package com.example.jewelrystore.exception;

public class InvalidOrderStatusException extends ApiException {
  public InvalidOrderStatusException(String message) {
    super(409, message);
  }
}
