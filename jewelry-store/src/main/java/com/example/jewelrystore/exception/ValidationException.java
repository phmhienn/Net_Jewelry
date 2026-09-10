package com.example.jewelrystore.exception;

public class ValidationException extends ApiException {
  public ValidationException(String message) {
    super(400, message);
  }
}
