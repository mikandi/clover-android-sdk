package com.clover.sdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Clover {
  private static final String TAG = "Clover";

  private static final int CLOVER_BUY_REQ_CODE = 0xc1eaf;
  private static final String CLOVER_PACKAGE = "com.clover.pay.android";
  private static final String ACTIVITY = "com.clover.pay.android.PurchaseActivity";
  private static final String ACTION_LINK_BASE = "clover://HandleActionLink?checkout&version=1&data=";

  /** Merchant ID associated with the Clover Account **/
  private final String merchantId;
  
  /** Context */
  private final Context context;

  /** Completion listener */
  private PurchaseListener listener;

  private Clover(Context context, String merchantId) {
    if (merchantId == null) {
      throw new IllegalArgumentException("A merchant ID is required");
    }
    this.merchantId = merchantId;
    this.context = context;
  }

  /**
   * Initialize the SDK with the current context and the Clover Mechant ID
   * @param context - current context or this.getBaseContext()
   * @param merchantId - Clover Merchant ID
   * @return an instance of the CloverSDK
   */
  public static Clover initializeSDK(Context context, String merchantId) {
    return new Clover(context, merchantId);
  }

  public CloverOrder.Builder createCloverOrderBuilder() {
    return new CloverOrder.Builder(this, merchantId);
  }

  //    TODO REMIND - Consider returning the entire Order object
  // by making a call to the server and fetching the order object if needed
  // OR using the one passed in the intent.

  /**
   * This needs to be hooked into the the caller Activity onActivityResult
   * @param requestCode
   * @param resultCode
   * @param data
   * @return order id
   */
  public void onResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == CLOVER_BUY_REQ_CODE && resultCode != 0) {

      String value = data.getStringExtra("data");
      try {
        String json = URLDecoder.decode(value, "UTF-8");
        String orderId = toOrderId(json);
        listener.onCompletion(orderId);
      } catch (UnsupportedEncodingException e) {
        listener.onFailure("Exception decoding object");
      } catch (JSONException e) {
        listener.onFailure("Exception parsing JSON");
      }
    }
  }

  /*package*/ static String toOrderId(String json) throws JSONException {
    JSONObject p = new JSONObject(json);
    JSONObject object = (JSONObject) p.get("order");
    return (String) object.get("id");
  }

  /**
   * @return boolean indicating whether clover is installed
   */
  public boolean hasCloverApp() {
    boolean useApp;
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(CLOVER_PACKAGE, 0);
      // maybe validate the app here
      useApp = info != null; // check signatures here
    } catch (PackageManager.NameNotFoundException ex) {
      useApp = false;
    }
    return useApp;
  }


  public void showDialog(Activity activity, CloverOrder cloverOrder, PurchaseListener listener) {
    new CloverOverlay(activity, cloverOrder, listener).show();
  }

  public void sendCloverIntent(CloverOrder cloverOrder, Activity activity, PurchaseListener listener) {
    this.listener = listener;
    Intent intent = null;
    if (hasCloverApp()) {
      intent = new Intent(Intent.ACTION_VIEW);
      intent.setComponent(new ComponentName(CLOVER_PACKAGE, ACTIVITY));
      try {
        intent.setData(Uri.parse(ACTION_LINK_BASE + URLEncoder.encode(cloverOrder.toJsonString(), "UTF-8")));
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
}
