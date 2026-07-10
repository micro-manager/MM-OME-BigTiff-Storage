package org.micromanager.mmomebigtiff;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Small shared Jackson helpers. */
final class JsonUtil {

   private JsonUtil() { }

   static final ObjectMapper MAPPER = new ObjectMapper();

   private static final TypeReference<Map<String, Object>> MAP_TYPE =
         new TypeReference<Map<String, Object>>() { };

   static String toJson(Object value) {
      try {
         return MAPPER.writeValueAsString(value);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   static Map<String, Object> parseObject(String json) {
      if (json == null) {
         return new LinkedHashMap<>();
      }
      try {
         return MAPPER.readValue(json, MAP_TYPE);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }
}
