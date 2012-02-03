package com.clover.sdk;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

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

  static CloverOrder parseCloverOrder(String json) throws Exception {
    final JSONObject object = new JSONObject(json);
    return parseJson(CloverOrder.class, object.getJSONObject("order"));
  }
  
  private static <T> T parseJson(Class<T> clazz, JSONObject object) throws IllegalAccessException, InstantiationException {
    T instance = clazz.newInstance();

    Field[] fields = clazz.getDeclaredFields();
    // Will work only on public fields
    for (Field field : fields) {
      if (Modifier.isPublic(field.getModifiers())) {
        Log.d("utils", "processing " + field.getName());
        try {
          setValue(field, object, instance);
        } catch (JSONException e) {
          // ignore this exception and keep going
        }
      }
    }
    return instance;
  }

  // Recursively find the fields and set the values
  // Note that this currently only supports the fields necessary to populate
  // the CloverOrder. Any changes to CloverOrder may require changes to this code.
  @SuppressWarnings("unchecked")
  private static <T> void setValue(Field field, JSONObject jsonObject, T instance)
      throws JSONException, IllegalAccessException, InstantiationException
  {
    if (!jsonObject.has(field.getName())) return;
    Class<?> clazz = field.getType();

    if (clazz == Integer.class || clazz == Integer.TYPE) {
      field.set(instance, jsonObject.getInt(field.getName()));
    } else if (clazz == Long.class || clazz == Long.TYPE) {
      field.set(instance, jsonObject.getLong(field.getName()));
    } else if (clazz == Double.class || clazz == Double.TYPE) {
      field.set(instance, jsonObject.getDouble(field.getName()));
    } else if (clazz == Boolean.class || clazz == Boolean.class) {
      field.set(instance, jsonObject.getBoolean(field.getName()));
    } else if (clazz == String.class) {
      field.set(instance, jsonObject.getString(field.getName()));
    } else if (clazz == List.class) {
      // we don't require this yet for CloverOrder
    } else if (clazz == Map.class) {
      // we don't require this yet for the CloverOrder
    } else if (clazz.isEnum()) {
      String value = jsonObject.getString(field.getName());
      try {
        Object enumValue = Enum.valueOf((Class<? extends Enum>) clazz, value);
        if (enumValue != null) field.set(instance, enumValue);
      } catch (Exception ignore) {}
    } else {
      Object o = parseJson(clazz, jsonObject.getJSONObject(field.getName()));
      field.set(instance, o);
    }
  }
}
