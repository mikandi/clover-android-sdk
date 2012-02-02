package com.clover.sdk;

public interface PurchaseListener {
  public void onCompletion(String orderId);
  public void onFailure(String message);
}
