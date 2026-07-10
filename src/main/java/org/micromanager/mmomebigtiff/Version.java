package org.micromanager.mmomebigtiff;

/**
 * On-disk format version, kept deliberately separate from the library artifact version
 * (following the NDTiffStorage convention). This describes the layout and metadata
 * conventions written by {@link OMEBigTiffStorage}, not the Maven version of the jar.
 */
public final class Version {

   private Version() { }

   /** OME-XML schema version written into the {@code ImageDescription} OME-XML. */
   public static final String OME_SCHEMA = "2016-06";

   /** OME-XML namespace URI. */
   public static final String OME_NS =
         "http://www.openmicroscopy.org/Schemas/OME/" + OME_SCHEMA;

   /** Layout/convention version of this library's on-disk output. */
   public static final int MAJOR = 1;
   public static final int MINOR = 0;

   /** {@code Software} tag value written into each TIFF file. */
   public static final String SOFTWARE = "MM-OME-BigTiff-Storage " + MAJOR + "." + MINOR;
}
