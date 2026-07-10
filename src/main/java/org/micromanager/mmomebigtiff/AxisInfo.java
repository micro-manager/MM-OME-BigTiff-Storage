package org.micromanager.mmomebigtiff;

import java.util.Collections;
import java.util.List;

/**
 * Describes one non-position axis of the dataset: its name, semantic type, physical scale
 * and (optionally) a fixed length. Used both to order the planes and to build the OME-XML
 * {@code Pixels}/{@code Channel} metadata.
 *
 * <p>The two spatial axes ("y" and "x") are always the last two axes of each plane and are
 * supplied implicitly by {@link OMEBigTiffStorage} from the image width/height; callers only
 * describe the non-spatial axes (time, channel, z) plus, optionally, the pixel size for y/x
 * via the config.
 *
 * <p>Because OME-TIFF's dimension model is exactly {@code Z}/{@code C}/{@code T}, each declared
 * non-position axis must have type {@link DimensionType#TIME}, {@link DimensionType#CHANNEL} or
 * {@link DimensionType#SPACE}; at most one axis of each type is allowed.
 */
public final class AxisInfo {

   private final String name;
   private final DimensionType type;
   private final String unit;
   private final double scale;
   private final Integer count;          // null = grow dynamically as images arrive
   private final List<Channel> channels; // only meaningful for a CHANNEL axis; may be null

   private AxisInfo(Builder b) {
      if (b.name == null || b.name.isEmpty()) {
         throw new IllegalArgumentException("Axis name must not be empty");
      }
      this.name = b.name;
      this.type = b.type == null ? DimensionType.OTHER : b.type;
      this.unit = b.unit;
      this.scale = b.scale;
      this.count = b.count;
      this.channels = b.channels == null ? null : Collections.unmodifiableList(b.channels);
   }

   public String getName() {
      return name;
   }

   public DimensionType getType() {
      return type;
   }

   public String getUnit() {
      return unit;
   }

   public double getScale() {
      return scale;
   }

   public Integer getCount() {
      return count;
   }

   public List<Channel> getChannels() {
      return channels;
   }

   public boolean isChannel() {
      return type == DimensionType.CHANNEL;
   }

   @Override
   public String toString() {
      return "AxisInfo{name='" + name + "', type=" + type + ", count=" + count + "}";
   }

   public static Builder builder(String name) {
      return new Builder(name);
   }

   public static final class Builder {
      private final String name;
      private DimensionType type;
      private String unit;
      private double scale = 1.0;
      private Integer count;
      private List<Channel> channels;

      public Builder(String name) {
         this.name = name;
      }

      public Builder type(DimensionType type) {
         this.type = type;
         return this;
      }

      public Builder unit(String unit) {
         this.unit = unit;
         return this;
      }

      /** Physical size represented by one step along this axis (e.g. micrometers per z slice). */
      public Builder scale(double scale) {
         this.scale = scale;
         return this;
      }

      /** Fixed length; omit (or null) to let the axis grow as images are written. */
      public Builder count(Integer count) {
         if (count != null && count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
         }
         this.count = count;
         return this;
      }

      public Builder channels(List<Channel> channels) {
         this.channels = channels;
         return this;
      }

      public AxisInfo build() {
         return new AxisInfo(this);
      }
   }
}
