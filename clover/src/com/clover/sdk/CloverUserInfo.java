package com.clover.sdk;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * UserInfo that can be optionally set by the Application
 * This information is only used during first purchase to pre-fill the information on the webView overlay
 */
public class CloverUserInfo {
  private String name;
  private String phoneNumber;
  private String email;

  public CloverUserInfo() {}

  /**
   * @param name of the user
   * @return UserInfo instance
   */
  public CloverUserInfo setFullName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @param phoneNumber of the user
   * @return UserInfo
   */
  public CloverUserInfo setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  /**
   * @param email address of the user
   * @return UserInfo
   */
  public CloverUserInfo setEmail(String email) {
    this.email = email;
    return this;
  }

  public void toJson(JSONObject dataJson) {
    try {
      JSONObject object = new JSONObject();
      if (name != null) object.put("fullName", name);
      if (phoneNumber != null) object.put("phoneNumber", phoneNumber);
      if (email != null) object.put("emailAddress", email);
      dataJson.put("userInfo", object);
    } catch (JSONException ignored) {
      ignored.printStackTrace();
    }
  }
}
