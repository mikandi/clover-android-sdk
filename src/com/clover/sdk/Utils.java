package com.clover.sdk;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class Utils {

  private static NumberFormat TWO_DEC = new DecimalFormat("0.00");

  // Class known for its static methods
  private Utils() {}

  static boolean isValidAmount(String amount) {
    if (amount == null) return false;
    try {
      Number n = TWO_DEC.parse(amount);
      return n != null && n.doubleValue() > 0.0d;
    } catch (ParseException e) {
      return false;
    }
  }

  /**
   * Util method
   * @param permissionTypes String[]
   * @return comma separated permission string
   */
  static String toPermissions(String[] permissionTypes) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < permissionTypes.length; i++) {
      sb.append(permissionTypes[i]);
      if (i < permissionTypes.length -1) sb.append(",");
    }
    return sb.toString();
  }
}
