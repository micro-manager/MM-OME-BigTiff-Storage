package org.micromanager.mmomebigtiff;

/**
 * The minimal image description a consumer needs before fetching pixels or full metadata:
 * dimensions, bit depth and whether the image is RGB.
 *
 * <p>Adapted from {@code org.micromanager.ndtiffstorage.EssentialImageMetadata}.
 */
public final class EssentialImageMetadata {

   private final int width;
   private final int height;
   private final int bitDepth;
   private final boolean rgb;

   public EssentialImageMetadata(int width, int height, int bitDepth, boolean rgb) {
      this.width = width;
      this.height = height;
      this.bitDepth = bitDepth;
      this.rgb = rgb;
   }

   public int getWidth() {
      return width;
   }

   public int getHeight() {
      return height;
   }

   public int getBitDepth() {
      return bitDepth;
   }

   public boolean isRGB() {
      return rgb;
   }

   @Override
   public String toString() {
      return "EssentialImageMetadata{width=" + width + ", height=" + height
            + ", bitDepth=" + bitDepth + ", rgb=" + rgb + "}";
   }
}
