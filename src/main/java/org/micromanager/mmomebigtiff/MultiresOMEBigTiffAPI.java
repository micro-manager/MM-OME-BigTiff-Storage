package org.micromanager.mmomebigtiff;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Multi-resolution (pyramid) extension of {@link OMEBigTiffAPI}. Each successive resolution
 * level is downsampled by a factor of two in x and y and stored as a TIFF {@code SubIFD} of the
 * full-resolution plane's IFD — the layout produced by {@code bfconvert --pyramid-resolutions}
 * and read by QuPath, Bio-Formats, tifffile and libtiff.
 *
 * <p>Because a TIFF plane's SubIFD array is written inline with the plane, the number of levels
 * is fixed for the lifetime of a file: {@link #setMaxResolutionLevel} may only raise it before
 * the first image is written. Lower-resolution data is produced automatically by 2×2 averaging
 * on {@code putImage} (or supplied by the caller through {@link OMEBigTiffStorageConfig}).
 */
public interface MultiresOMEBigTiffAPI extends OMEBigTiffAPI {

   /** Number of resolution levels currently in the pyramid. */
   int getNumResLevels();

   /**
    * Ensure the pyramid has at least this many levels. For OME-BigTIFF this is only permitted
    * before the first image has been written (the level count is baked into every plane's
    * SubIFD array); calling it after writing has begun throws {@link IllegalStateException}.
    *
    * @param numLevels desired number of resolution levels (>= current)
    */
   void setMaxResolutionLevel(int numLevels);

   /** Get a single image at the given resolution level. */
   OMEBigTiffImage getImage(Map<String, Object> axes, int resolutionLevel);

   /** Whether an image exists at the given resolution level. */
   boolean hasImage(Map<String, Object> axes, int resolutionLevel);

   /** Essential metadata at the given resolution level. */
   EssentialImageMetadata getEssentialImageMetadata(Map<String, Object> axes, int resolutionLevel);
}
