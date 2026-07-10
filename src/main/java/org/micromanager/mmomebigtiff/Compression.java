package org.micromanager.mmomebigtiff;

/**
 * Compression codecs supported by the OME-BigTIFF writer. Kept intentionally small: both are
 * understood by every mainstream TIFF reader (libtiff, tifffile, Bio-Formats, ImageJ).
 *
 * <ul>
 *   <li>{@link #NONE} — uncompressed; lowest latency, best for streaming acquisition.</li>
 *   <li>{@link #DEFLATE} — Adobe/zlib Deflate (TIFF compression code 8).</li>
 * </ul>
 */
public enum Compression {
   NONE("none", 1),
   DEFLATE("deflate", 8);

   /** TIFF {@code Compression} tag value. */
   private final int tiffCode;
   private final String id;

   Compression(String id, int tiffCode) {
      this.id = id;
      this.tiffCode = tiffCode;
   }

   public int tiffCode() {
      return tiffCode;
   }

   public String getId() {
      return id;
   }

   public static Compression fromId(String id) {
      for (Compression c : values()) {
         if (c.id.equalsIgnoreCase(id)) {
            return c;
         }
      }
      throw new IllegalArgumentException("Unknown compression: " + id);
   }

   public static Compression fromTiffCode(int code) {
      for (Compression c : values()) {
         if (c.tiffCode == code) {
            return c;
         }
      }
      throw new IllegalArgumentException("Unsupported TIFF compression code: " + code
            + " (supported: 1=none, 8=deflate)");
   }

   @Override
   public String toString() {
      return id;
   }
}
