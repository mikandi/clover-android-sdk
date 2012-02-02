package com.clover.sdk;

public interface OrderListener {
  public void onCompletion(String orderId);
  public void onFailure(String message);
}
