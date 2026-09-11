package com.example.jewelrystore.exception;

public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(403, message);
  }
}
