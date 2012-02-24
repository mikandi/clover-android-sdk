package com.clover.sdk.impl;

import com.clover.sdk.CloverOrder;

/** Internal representation of the CloverOrder **/
public class CloverOrderInternal {
  public String id;
  public CloverOrder.Status status;
  public String amount;
  public String title;
  public Long created_on;
  public Permissions permissions;
  public String client_order_id;

  public static class Permissions {
    public String full_name;
    public EmailAddress email_address;
    public ShippingAddress shipping_address;
  }

  public static class EmailAddress {
    public String email;
    public boolean is_verified;
  }

  public static class ShippingAddress {
    public String name;
    public String address_1;
    public String address_2;
    public String address_3;
    public String city;
    public String state;
    public String zip;
    public String country;
    public boolean is_verified;
  }
}
