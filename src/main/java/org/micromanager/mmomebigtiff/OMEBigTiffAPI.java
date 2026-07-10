package org.micromanager.mmomebigtiff;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Minimal storage API for streaming multi-dimensional images to OME-BigTIFF, modeled on
 * {@code org.micromanager.ndtiffstorage.NDTiffAPI}. Multi-resolution/pyramid operations live
 * in {@link MultiresOMEBigTiffAPI}.
 *
 * <p>Images are addressed by an axes map ({@code axisName -> Integer|String}). Writing is
 * asynchronous on a dedicated thread; {@link #putImage} returns a {@link Future} that
 * completes when the image is durably written. Reads are served immediately from an in-memory
 * write-pending buffer and fall back to disk, so reading during acquisition is safe.
 */
public interface OMEBigTiffAPI extends AutoCloseable {

   /**
    * Add an image to storage (resolution level 0). If the pyramid has more than one level and
    * auto-downsampling is enabled, the lower-resolution levels are computed and written too.
    *
    * @param pixels      primitive pixel array ({@code byte[]}, {@code short[]}, {@code float[]})
    * @param metadataJson per-image metadata as a JSON string (may be null)
    * @param axes        axis name to position (Integer or String)
    * @param rgb         whether the image is RGB (unsupported in v1)
    * @param bitDepth    bits per pixel component (8/16/32)
    * @param imageHeight image height in pixels
    * @param imageWidth  image width in pixels
    * @return a future completing when the image is written
    */
   Future<Void> putImage(Object pixels, String metadataJson, Map<String, Object> axes,
                         boolean rgb, int bitDepth, int imageHeight, int imageWidth);

   /** Whether the dataset is finished writing and now read-only. */
   boolean isFinished();

   /** Re-throw any exception that occurred on the writer thread. */
   void checkForWritingException() throws Exception;

   /** The dataset summary metadata as a JSON string (supplied at construction), or null. */
   String getSummaryMetadata();

   /** Store an arbitrary custom-metadata blob under {@code key}. */
   void setCustomMetadata(String key, String json);

   /** Retrieve a custom-metadata blob previously stored under {@code key}, or null. */
   String getCustomMetadata(String key);

   /** Signal that no more images will be written; the dataset becomes read-only. */
   void finishedWriting();

   /** Path to the dataset root folder, or null if not on disk. */
   String getDiskLocation();

   /** A unique name for this dataset instance. */
   String getUniqueAcqName();

   /** [xMin, yMin, xMax, yMax] pixel bounds of acquired data. */
   int[] getImageBounds();

   /** Get a single image (resolution level 0). */
   OMEBigTiffImage getImage(Map<String, Object> axes);

   /** Get per-image metadata JSON for the image with the given axes, or null. */
   String getImageMetadata(Map<String, Object> axes);

   /** Essential metadata (width/height/bitDepth/rgb) without loading pixels. */
   EssentialImageMetadata getEssentialImageMetadata(Map<String, Object> axes);

   /** Whether an image with the given axes exists. */
   boolean hasImage(Map<String, Object> axes);

   /** The set of axis positions for every image in the dataset. */
   Set<Map<String, Object>> getAxesSet();

   /** Number of images waiting in the writer queue. */
   int getWritingQueueTaskSize();

   /** Maximum size of the writer queue. */
   int getWritingQueueTaskMaxSize();

   /** Release all resources. */
   @Override
   void close();

   /** Release all resources and block until done. */
   void closeAndWait() throws InterruptedException;
}
