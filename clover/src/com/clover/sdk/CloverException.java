package com.clover.sdk;

public class CloverException extends RuntimeException {
  public String code;
  public String message;

  public CloverException() {}
  public CloverException(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
