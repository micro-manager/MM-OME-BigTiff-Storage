package org.micromanager.mmomebigtiff;

/**
 * Physical/semantic type of a dimension axis, mapped both to the OME-XML dimension model
 * (Z/C/T) and to how {@link OMEBigTiffStorage} lays out planes.
 */
public enum DimensionType {
   /** A spatial (z) axis; maps to the OME-XML {@code Z} dimension. */
   SPACE,
   /** A time axis; maps to the OME-XML {@code T} dimension. */
   TIME,
   /** A channel axis; maps to the OME-XML {@code C} dimension. */
   CHANNEL,
   /** A stage-position axis; represented as separate OME-BigTIFF files, not a plane dimension. */
   POSITION,
   /** An axis with no standard OME-XML dimension (rejected by the OME-TIFF writer). */
   OTHER;

   /** OME-XML dimension letter ({@code Z}/{@code C}/{@code T}), or null if it has none. */
   public String omeDimension() {
      switch (this) {
         case SPACE:   return "Z";
         case TIME:    return "T";
         case CHANNEL: return "C";
         default:      return null; // POSITION and OTHER have no plane dimension
      }
   }
}
