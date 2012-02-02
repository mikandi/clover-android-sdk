package com.clover.sdk;

/**
 * Class that identified the kind of permission desired
 * An application can request one or more of the following permissions
 * for example: FULL_NAME, EMAIL_ADDRESS or just FULL_NAME, SHIPPING_ADDRESS
 * Once granted by the user, these bits of information are returned to the user.
 */
public enum PermissionType {
  FULL_NAME, // Full name of the user
  EMAIL_ADDRESS, // Email address of the user
  SHIPPING_ADDRESS // The address where the goods for this particular purchase should be sent
}
