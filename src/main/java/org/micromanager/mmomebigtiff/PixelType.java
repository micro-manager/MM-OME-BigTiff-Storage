package org.micromanager.mmomebigtiff;

/**
 * Pixel formats the storage understands, and their mapping to OME-XML {@code Pixels/@Type} and
 * TIFF {@code SampleFormat}.
 *
 * <p>v1 supports single-component grayscale in 8-, 16- and 32-bit forms. RGB is not yet
 * supported (see README).
 */
public enum PixelType {
   GRAY8("uint8", 1, PixelType.SAMPLE_FORMAT_UINT),
   GRAY16("uint16", 2, PixelType.SAMPLE_FORMAT_UINT),
   GRAY32("float", 4, PixelType.SAMPLE_FORMAT_FLOAT);

   /** TIFF SampleFormat tag value for unsigned integer samples. */
   public static final int SAMPLE_FORMAT_UINT = 1;
   /** TIFF SampleFormat tag value for IEEE floating-point samples. */
   public static final int SAMPLE_FORMAT_FLOAT = 3;

   private final String omeType;
   private final int bytesPerPixel;
   private final int sampleFormat;

   PixelType(String omeType, int bytesPerPixel, int sampleFormat) {
      this.omeType = omeType;
      this.bytesPerPixel = bytesPerPixel;
      this.sampleFormat = sampleFormat;
   }

   /** OME-XML {@code Pixels/@Type} string, e.g. {@code "uint16"}. */
   public String omeType() {
      return omeType;
   }

   public int bytesPerPixel() {
      return bytesPerPixel;
   }

   public int sampleFormat() {
      return sampleFormat;
   }

   public int bitDepth() {
      return bytesPerPixel * 8;
   }

   /**
    * Resolve a pixel type from the {@code (rgb, bitDepth)} pair used by the {@code putImage}
    * API (which mirrors NDTiffStorage's signature).
    *
    * @param rgb      whether the image has multiple colour components
    * @param bitDepth nominal bits per component (8, 16 or 32)
    */
   public static PixelType of(boolean rgb, int bitDepth) {
      if (rgb) {
         throw new UnsupportedOperationException(
               "RGB images are not supported in this version of MM-OME-BigTiff-Storage.");
      }
      if (bitDepth <= 8) {
         return GRAY8;
      }
      if (bitDepth <= 16) {
         return GRAY16;
      }
      if (bitDepth <= 32) {
         return GRAY32;
      }
      throw new IllegalArgumentException("Unsupported bit depth: " + bitDepth);
   }

   /**
    * Resolve a pixel type from an OME-XML {@code Pixels/@Type} string (e.g. {@code "uint16"}).
    *
    * @throws IllegalArgumentException for types this library cannot represent, rather than
    *         silently mis-reading the data
    */
   public static PixelType fromOmeType(String type) {
      for (PixelType t : values()) {
         if (t.omeType.equalsIgnoreCase(type)) {
            return t;
         }
      }
      // OME-XML uses "float" for 32-bit float; also accept "float32" defensively.
      if ("float32".equalsIgnoreCase(type)) {
         return GRAY32;
      }
      throw new IllegalArgumentException("Unsupported OME pixel type: " + type
            + " (supported: uint8, uint16, float)");
   }

   /** Number of pixels represented by a primitive pixel array of this type. */
   public int pixelCount(Object pixels) {
      switch (this) {
         case GRAY8:  return ((byte[]) pixels).length;
         case GRAY16: return ((short[]) pixels).length;
         case GRAY32: return ((float[]) pixels).length;
         default:     throw new IllegalStateException();
      }
   }
}
