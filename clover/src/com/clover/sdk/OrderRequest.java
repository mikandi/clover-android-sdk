package com.clover.sdk;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OrderRequest {

  private static final String TAG = OrderRequest.class.getSimpleName();

  private final Clover instance;
  private final String account;
  private final String title;
  private final String amount;
  private final String permissions;
  private final String type;
  private final String clientOrderId;
  private final String imageUrl;

  private OrderRequest(Clover instance, String account, String title, String amount, String permissions, String type, String clientOrderId, String imageUrl) {
    if (title == null) throw new IllegalArgumentException("Missing required field title");

    this.instance = instance;
    this.account = account;
    this.title = title;
    this.amount = amount;
    this.permissions = permissions;
    this.type = type;
    this.clientOrderId = clientOrderId;
    this.imageUrl = imageUrl;
  }
  
  public String toJsonString() {
    JSONObject json = toJson();
    return json.toString();
  }
  
  public JSONObject toJson() {
    JSONObject dataObject = new JSONObject();
    try {
      JSONObject properties = new JSONObject();
      properties.put("account", account);
      properties.put("amount", amount);
      properties.put("permissions", permissions);
      properties.put("title", title);
      properties.put("type", type);
      if (clientOrderId != null) {
        properties.put("client_order_id", clientOrderId);
      }
      if (imageUrl != null) {
        properties.put("image", imageUrl);
      }
      dataObject.put("buttonProperties", properties);
    } catch (JSONException e) {
      Log.e(TAG, "Exception creating a json object ", e);
    }
    return dataObject;
  }

  /**
   * Helper Class for creating a CloverOrder
   */
  public static class Builder {
    private String title;
    private String amount;
    private List<String> permissionTypes;
    private final Clover instance;
    private String account;
    private String type = "buy";
    private String clientOrderId;
    private String imageUrl;

    Builder(Clover instance, String account) {
      this.instance = instance;
      this.account = account;
      this.permissionTypes = new LinkedList<String>();
    }

    /**
     * @param title of the purchase
     * @return Builder instance
     */
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    /**
     * @param amount to authorize for this purchase
     * @return Builder instance
     */
    public Builder setAmount(String amount) {
      if (!Utils.isValidAmount(amount)) {
        throw new IllegalArgumentException("Invalid amount");
      }
      this.amount = amount;
      return this;
    }

    /**
     * @param permission to add
     * @return Builder instance
     */
    public Builder addPermission(String permission) {
      this.permissionTypes.add(permission);
      return this;
    }

    /**
     * @param permissions to be used
     * @return Builder instance
     */
    public Builder setPermissions(String[] permissions) {
      if (permissions == null) throw new IllegalArgumentException("Permissions cannot be null");
      this.permissionTypes.addAll(Arrays.asList(permissions));
      return this;
    }

    /**
     * @param imageUrl of the item to purchase
     * @return Builder instance
     */
    public Builder setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
      return this;
    }

    /**
     * @param id associated with this item. This can be an opaque string that is passed back verbatim with the response.
     * @return Builder instance
     */
    public Builder setClientOrderId(String id) {
      this.clientOrderId = id;
      return this;
    }

    /**
     * @return CloverOrder instance
     */
    public OrderRequest build() {
      return new OrderRequest(instance, account, title, amount,
          Utils.toPermissions(permissionTypes.toArray(new String[permissionTypes.size()])), type,
          clientOrderId, imageUrl);
    }
  }
}
