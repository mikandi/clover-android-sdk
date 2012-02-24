package com.clover.sdk;

import com.clover.sdk.impl.CloverOrderInternal;

public class CloverOrder {
  public String id;
  public Status status;
  public String amount;
  public String title;
  public Long createdOn;
  public Permissions permissions;
  public String clientOrderId;

  public CloverOrder(CloverOrderInternal delegate) {
    this.id = delegate.id;
    this.status = delegate.status;
    this.amount = delegate.amount;
    this.title = delegate.title;
    this.createdOn = delegate.created_on;
    this.permissions = delegate.permissions == null ? null : new Permissions(delegate.permissions);
    this.clientOrderId = delegate.client_order_id;
  }

  public static class Permissions {
    public String fullName;
    public EmailAddress emailAddress;
    public ShippingAddress shippingAddress;

    Permissions(CloverOrderInternal.Permissions permissions) {
      if (permissions != null) {
        this.fullName = permissions.full_name;
        this.emailAddress = permissions.email_address == null ? null :
            new EmailAddress(permissions.email_address);
        this.shippingAddress = permissions.shipping_address == null ? null :
            new ShippingAddress(permissions.shipping_address);
      }
    }
  }

  public static class EmailAddress {
    public final String email;
    public final boolean isVerified;

    EmailAddress(CloverOrderInternal.EmailAddress emailAddress) {
      final boolean noAddress = emailAddress == null;
      this.email = noAddress ? null : emailAddress.email;
      this.isVerified = emailAddress != null && emailAddress.is_verified;
    }
  }

  public static class ShippingAddress {
    public String name;
    public String address1;
    public String address2;
    public String address3;
    public String city;
    public String state;
    public String zip;
    public String country;
    public boolean isVerified;

    ShippingAddress(CloverOrderInternal.ShippingAddress shippingAddress) {
      if (shippingAddress != null) {
        this.name = shippingAddress.name;
        this.address1 = shippingAddress.address_1;
        this.address2 = shippingAddress.address_2;
        this.address3 = shippingAddress.address_3;
        this.city = shippingAddress.city;
        this.state = shippingAddress.state;
        this.zip = shippingAddress.zip;
        this.country = shippingAddress.country;
        this.isVerified = shippingAddress.is_verified;
      }
    }
  }

  public static enum Status {ordered, accepted, refunded, rejected, cancelled, authorized}
}
