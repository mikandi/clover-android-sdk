package com.clover.sdk;

/**
 * Callbacks associated with an order
 */
public interface CloverOrderListener {

  /**
   * @param order authorized by Clover either via the Overlay or the App
   */
  public void onOrderAuthorized(CloverOrder order);

  /**
   * Called on an user action specifically either of:
   * 1. The user hits Cancel on the web overlay
   * 2. The user hits the back button in the Clover App
   * 3. The user hits the cancel button in the Clover App
   */
  public void onCancel();

  /**
   * Callback to indicate a failure indicating either an order request problem such
   * as invalid permissions or a transport failure.
   * @param th indicating the type of the failure
   */
  public void onFailure(Throwable th);
}
