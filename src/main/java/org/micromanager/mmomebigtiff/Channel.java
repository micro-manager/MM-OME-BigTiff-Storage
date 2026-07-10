package org.micromanager.mmomebigtiff;

/**
 * Optional descriptive metadata for one channel, propagated to the OME-XML {@code Channel}
 * elements of the image.
 *
 * <p>Adapted (trimmed) from the sibling {@code MM-OME-Zarr-Storage} project.
 */
public final class Channel {

   private final String name;
   private final String color;   // hex RGB, e.g. "#00FF00", or null
   private final Integer excitationWavelengthNm;
   private final Integer emissionWavelengthNm;

   private Channel(Builder b) {
      if (b.name == null || b.name.isEmpty()) {
         throw new IllegalArgumentException("Channel name must not be empty");
      }
      this.name = b.name;
      this.color = b.color;
      this.excitationWavelengthNm = b.excitationWavelengthNm;
      this.emissionWavelengthNm = b.emissionWavelengthNm;
   }

   /** Convenience constructor for a name-only channel. */
   public Channel(String name) {
      this(new Builder(name));
   }

   public String getName() {
      return name;
   }

   public String getColor() {
      return color;
   }

   public Integer getExcitationWavelengthNm() {
      return excitationWavelengthNm;
   }

   public Integer getEmissionWavelengthNm() {
      return emissionWavelengthNm;
   }

   public static Builder builder(String name) {
      return new Builder(name);
   }

   public static final class Builder {
      private final String name;
      private String color;
      private Integer excitationWavelengthNm;
      private Integer emissionWavelengthNm;

      public Builder(String name) {
         this.name = name;
      }

      /** Hex RGB string, e.g. {@code "#00FF00"}. */
      public Builder color(String hex) {
         this.color = hex;
         return this;
      }

      public Builder excitationWavelengthNm(int nm) {
         this.excitationWavelengthNm = nm;
         return this;
      }

      public Builder emissionWavelengthNm(int nm) {
         this.emissionWavelengthNm = nm;
         return this;
      }

      public Channel build() {
         return new Channel(this);
      }
   }
}
