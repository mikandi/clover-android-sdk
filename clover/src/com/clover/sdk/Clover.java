package com.clover.sdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import com.clover.sdk.impl.CloverOverlay;
import com.clover.sdk.impl.Utils;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Clover {
  private static final String TAG = "Clover";

  /** The request code sent with a startActivityForResult(..) */
  private static final int CLOVER_BUY_REQ_CODE = 0xc1eaf; // 794287
  /** The Clover package name */
  private static final String CLOVER_PACKAGE = "com.clover.pay.android";
  /** Activity for the purchase flow */
  private static final String ACTIVITY = "com.clover.pay.android.PurchaseActivity";

  private static final String ACTION_LINK_BASE = "clover://HandleActionLink?checkout&version=1&data=";

  /** Base version that supports checkout **/
  private static final int BASE_CHECKOUT_VERSION = 27;

  /** base ping version  */ 
  private static final int BASE_PING_BROADCAST_VERSION = 35;

  private static final String CHECKOUT = "checkout";
  private static final String PING_RECEIVER = "com.clover.pay.android.PingBroadcastReceiver";
  private static final String PING_ACTION = "com.clover.pay.android.intent.PING";

  /** Merchant ID associated with the Clover Account **/
  private final String merchantId;
  
  /** Context */
  private final Context context;

  /** Completion listener */
  private CloverOrderListener listener;

  /** Optional userInfo */
  private final CloverUserInfo userInfo;

  private Clover(Context context, String merchantId) {
    if (merchantId == null) {
      throw new IllegalArgumentException("A merchant ID is required");
    }
    this.merchantId = merchantId;
    this.context = context;
    this.userInfo = new CloverUserInfo();
    sendPingIntent();
  }

  /**
   * Initialize the SDK with the current context and the Clover Mechant ID
   * @param context - current context or this.getBaseContext()
   * @param merchantId - Clover Merchant ID
   * @return an instance of the CloverSDK
   */
  public static Clover init(Context context, String merchantId) {
    return new Clover(context, merchantId);
  }

  /**
   * Optional Information about the User that is used only in case Clover is not installed on the device.
   * This information is used to pre-fill the web overlay used for first time purchase.
   * It is highly recommended to provide as much information as possible to increase conversion by reducing
   * the burden on users to type in this data in the first purchase web view.
   * example Usage:
   * <code>
   * Clover.createFirstPurchaseInfo().setFullName("Full_Name").setPhoneNumber("555-111-2222").setEmail("foo@example.com");
   * </code>
   * @return UserInfo
   */
  public CloverUserInfo createFirstPurchaseInfo() {
    return userInfo;
  }


  /**
   * Create an OrderRequest object which can then be filled in with the necessary properties
   * such as Title, Amount, etc.
   *
   * example:
   * <code>
   *   OrderRequest request = cloverInstance.createOrderRequestBuilder().setTitle(..).setAmount("1.00").addPermission("full_name")...
   *      .addPermission("email_address").build();
   * </code>
   * @return OrderRequest.Builder instance
   */
  public CloverOrderRequest.Builder createOrderRequestBuilder() {
    return new CloverOrderRequest.Builder(this, merchantId);
  }

  /**
   * Authorize an OrderRequest object and receive any Callbacks on the OrderListener instance passed in.
   * @param activity instance processing the request.
   *                 This activity is used as the base for startActivityForResult if the Clover App is present on the device.
   *                 In its absence, a Webview overlay/Dialog is created with the activity as the context
   * @param orderRequest to be processed by Clover
   * @param orderListener to get all the callbacks
   */
  public void authorizeOrder(Activity activity, CloverOrderRequest orderRequest, CloverOrderListener orderListener) {
    if (activity == null) throw new IllegalArgumentException("Activity is required");
    if (orderRequest == null) throw new IllegalArgumentException("An orderRequest is required");
    if (orderListener == null) throw new IllegalArgumentException("An orderListener is required");

    sendCloverIntent(orderRequest, activity, orderListener);
  }

  /**
   * This needs to be hooked into the the callers activity:
   *
   * Activity#onActivityResult(int requestCode, int resultCode, Intent data)
   *
   * @param requestCode
   * @param resultCode
   * @param data
   * @return boolean indicating whether this event was handled by Clover SDK
   */
  public boolean onResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CLOVER_BUY_REQ_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        String value = data.getStringExtra("data");
        try {
          String json = URLDecoder.decode(value, "UTF-8");
          CloverOrder order = Utils.parseCloverOrder(json);
          listener.onOrderAuthorized(order);
        } catch (UnsupportedEncodingException e) {
          listener.onFailure(e);
        } catch (JSONException e) {
          listener.onFailure(e);
        } catch (Exception e) {
          listener.onFailure(e);
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        // An explicit back button or Cancel was called
        if (listener != null) listener.onCancel();
      }
      return true;
    }
    return false;
  }

  /**
   * @return boolean indicating whether clover is installed
   */
  public boolean hasCloverApp() {
    boolean useApp;
    try {
      PackageInfo info = getCloverPkgInfo();
      // maybe validate the app here
      useApp = info != null && info.versionCode >= BASE_CHECKOUT_VERSION ; // check signatures here
    } catch (PackageManager.NameNotFoundException ex) {
      useApp = false;
    }
    return useApp;
  }

  private PackageInfo getCloverPkgInfo() throws PackageManager.NameNotFoundException {
    return context.getPackageManager().getPackageInfo(CLOVER_PACKAGE, PackageManager.GET_RECEIVERS);
  }


  /**
   * In the absence of a Clover App that supports the CHECKOUT_VERSION, we show the dialog
   * as an overlay using the activity
   * @param activity to use as the parent of the dialog
   * @param orderRequest request
   * @param listener listener for callbacks
   */
  private void showDialog(Activity activity, CloverOrderRequest orderRequest, CloverOrderListener listener) {
    new CloverOverlay(activity, orderRequest, userInfo, listener).show();
  }

  private void sendCloverIntent(CloverOrderRequest cloverOrder, Activity activity, CloverOrderListener listener) {
    this.listener = listener;
    Intent intent = null;
    if (hasCloverApp()) {
      intent = new Intent(Intent.ACTION_VIEW);
      intent.setComponent(new ComponentName(CLOVER_PACKAGE, ACTIVITY));
      try {
        intent.setData(Uri.parse(ACTION_LINK_BASE + URLEncoder.encode(cloverOrder.toJsonString(), "UTF-8")));
        intent.putExtra(CHECKOUT, cloverOrder.asBundle());
      } catch (UnsupportedEncodingException e) {
        Log.e(TAG, "Encoding error ", e);
      } catch (ActivityNotFoundException e) {
        Log.e(TAG, "Activity not found ", e);
      }
    }
    if (intent != null) {
      activity.startActivityForResult(intent, CLOVER_BUY_REQ_CODE);
    } else {
      showDialog(activity, cloverOrder, listener);
    }
  }

  /**
   * Send a ping to our app to ensure it is inited
   */
  private void sendPingIntent() {
    try {
      PackageInfo info = getCloverPkgInfo();
      if (info != null && info.versionCode >= BASE_PING_BROADCAST_VERSION) {
        ActivityInfo[] receivers = info.receivers;
        if (receivers != null) {
          boolean hasPing = false;
          for (int i = 0; i < receivers.length; i++) {
            if (PING_RECEIVER.equals(receivers[i].name)) {
              hasPing = true;
              break;
            }
          }
          if (hasPing) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(CLOVER_PACKAGE, PING_RECEIVER));
            intent.setAction(PING_ACTION);
            context.sendBroadcast(intent);
          }
        }
      }
    } catch (Exception ignore) {
      // ignore this exception
    }
  }

}
