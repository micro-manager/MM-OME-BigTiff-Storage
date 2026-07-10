package org.micromanager.mmomebigtiff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Canonical serialization of an axes map ({@code axisName -> position}) to a deterministic
 * string that is used as the key into all internal indices, following the approach in
 * {@code NDTiffStorage} (sorted keys so ordering is irrelevant).
 *
 * <p>Axis values may be {@link Integer} or {@link String} (matching NDTiff's format v3.2+).
 */
public final class AxesKey {

   private AxesKey() { }

   /** Serialize an axes map to a deterministic JSON string with sorted keys. */
   public static String serialize(Map<String, Object> axes) {
      // TreeMap => keys sorted; Jackson preserves that order for a deterministic string.
      return JsonUtil.toJson(new TreeMap<>(axes));
   }

   /** Inverse of {@link #serialize(Map)}. */
   public static Map<String, Object> deserialize(String key) {
      Map<String, Object> parsed = JsonUtil.parseObject(key);
      // Normalize numeric values that Jackson may widen to Long back to Integer where safe.
      Map<String, Object> out = new LinkedHashMap<>();
      for (Map.Entry<String, Object> e : parsed.entrySet()) {
         Object v = e.getValue();
         if (v instanceof Long && (Long) v >= Integer.MIN_VALUE && (Long) v <= Integer.MAX_VALUE) {
            out.put(e.getKey(), ((Long) v).intValue());
         } else {
            out.put(e.getKey(), v);
         }
      }
      return out;
   }

   /** Return the integer value for an axis, or a default if absent/non-integer. */
   public static int intValue(Map<String, Object> axes, String axis, int dflt) {
      Object v = axes.get(axis);
      if (v instanceof Number) {
         return ((Number) v).intValue();
      }
      return dflt;
   }
}
