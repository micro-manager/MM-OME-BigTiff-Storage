package org.micromanager.mmomebigtiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a new {@link OMEBigTiffStorage} dataset.
 *
 * <p>All fields have sensible defaults; the common case is
 * {@code new OMEBigTiffStorageConfig()} plus perhaps a channel/pixel-size description for richer
 * OME metadata. Non-position axes may be declared up front (bounded, best metadata) or
 * discovered dynamically from the first {@code putImage} call.
 */
public final class OMEBigTiffStorageConfig {

   /**
    * Ordered non-position axes (excluding the trailing spatial y/x, which are implicit).
    * If empty, the axis set and order are inferred from the first image's axes map, using
    * the canonical order (time, channel, z). Each axis must map to an OME-XML dimension
    * (time→T, channel→C, z/space→Z); at most one axis per dimension.
    */
   private List<AxisInfo> axes = new ArrayList<>();

   /** Name of the axis that maps to separate OME-BigTIFF files (one file per position). */
   private String positionAxis = "position";

   /** Physical pixel size for the y and x axes (in {@link #spatialUnit}). */
   private double pixelSizeY = 1.0;
   private double pixelSizeX = 1.0;
   private String spatialUnit = "micrometer";

   private Compression compression = Compression.NONE;

   /** Number of pyramid resolution levels (1 = no pyramid). Each level halves y and x. */
   private int numResolutionLevels = 1;

   /** If true, {@code putImage} auto-computes and writes all pyramid levels by 2x2 averaging. */
   private boolean autoDownsample = true;

   /** Bounded size of the writer queue; a full queue applies back-pressure to producers. */
   private int savingQueueSize = 50;

   public List<AxisInfo> getAxes() {
      return axes;
   }

   public OMEBigTiffStorageConfig axes(List<AxisInfo> axes) {
      this.axes = new ArrayList<>(axes);
      return this;
   }

   public OMEBigTiffStorageConfig addAxis(AxisInfo axis) {
      this.axes.add(axis);
      return this;
   }

   public String getPositionAxis() {
      return positionAxis;
   }

   public OMEBigTiffStorageConfig positionAxis(String positionAxis) {
      this.positionAxis = positionAxis;
      return this;
   }

   public double getPixelSizeY() {
      return pixelSizeY;
   }

   public double getPixelSizeX() {
      return pixelSizeX;
   }

   public OMEBigTiffStorageConfig pixelSize(double sizeYAndX) {
      this.pixelSizeY = sizeYAndX;
      this.pixelSizeX = sizeYAndX;
      return this;
   }

   public OMEBigTiffStorageConfig pixelSize(double sizeY, double sizeX) {
      this.pixelSizeY = sizeY;
      this.pixelSizeX = sizeX;
      return this;
   }

   public String getSpatialUnit() {
      return spatialUnit;
   }

   public OMEBigTiffStorageConfig spatialUnit(String spatialUnit) {
      this.spatialUnit = spatialUnit;
      return this;
   }

   public Compression getCompression() {
      return compression;
   }

   public OMEBigTiffStorageConfig compression(Compression compression) {
      this.compression = compression;
      return this;
   }

   public int getNumResolutionLevels() {
      return numResolutionLevels;
   }

   public OMEBigTiffStorageConfig numResolutionLevels(int levels) {
      if (levels < 1) {
         throw new IllegalArgumentException("numResolutionLevels must be >= 1");
      }
      this.numResolutionLevels = levels;
      return this;
   }

   public boolean isAutoDownsample() {
      return autoDownsample;
   }

   public OMEBigTiffStorageConfig autoDownsample(boolean autoDownsample) {
      this.autoDownsample = autoDownsample;
      return this;
   }

   public int getSavingQueueSize() {
      return savingQueueSize;
   }

   public OMEBigTiffStorageConfig savingQueueSize(int savingQueueSize) {
      this.savingQueueSize = savingQueueSize;
      return this;
   }
}
