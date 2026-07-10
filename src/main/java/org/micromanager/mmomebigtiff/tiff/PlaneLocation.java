package org.micromanager.mmomebigtiff.tiff;

/**
 * Records where a single multi-resolution plane's pixel strips live inside an OME-BigTIFF file:
 * one file offset, on-disk byte count and spatial extent per pyramid level (level 0 = full
 * resolution). Populated by {@link TiffPyramidWriter} as planes are written and by
 * {@link TiffPyramidReader} when an existing file is reopened; used by the storage's read path
 * to fetch pixels without re-parsing the IFDs.
 */
public final class PlaneLocation {

   /** File offset of each level's pixel strip (index 0 = full resolution). */
   public final long[] offset;
   /** On-disk byte count of each level's strip (compressed length if compressed). */
   public final long[] byteCount;
   /** Pixel width of each level. */
   public final int[] width;
   /** Pixel height of each level. */
   public final int[] height;

   public PlaneLocation(int numLevels) {
      this.offset = new long[numLevels];
      this.byteCount = new long[numLevels];
      this.width = new int[numLevels];
      this.height = new int[numLevels];
   }

   public int numLevels() {
      return offset.length;
   }
}
