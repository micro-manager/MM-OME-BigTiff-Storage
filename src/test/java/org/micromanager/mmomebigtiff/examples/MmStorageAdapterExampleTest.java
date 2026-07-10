package org.micromanager.mmomebigtiff.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.micromanager.mmomebigtiff.AxisInfo;
import org.micromanager.mmomebigtiff.DimensionType;
import org.micromanager.mmomebigtiff.OMEBigTiffImage;
import org.micromanager.mmomebigtiff.OMEBigTiffStorageConfig;
import org.micromanager.mmomebigtiff.examples.MmStorageAdapterExample.MmCoords;
import org.micromanager.mmomebigtiff.examples.MmStorageAdapterExample.MmImage;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the illustrative adapter end-to-end to keep the documented mapping honest. */
public class MmStorageAdapterExampleTest {

   private static final class Coords implements MmCoords {
      private final java.util.Map<String, Integer> idx = new java.util.LinkedHashMap<>();

      Coords set(String axis, int v) {
         idx.put(axis, v);
         return this;
      }

      @Override
      public Iterable<String> getAxes() {
         return idx.keySet();
      }

      @Override
      public int getIndex(String axis) {
         return idx.getOrDefault(axis, -1);
      }
   }

   private static final class Image implements MmImage {
      final short[] pix;
      final int w;
      final int h;
      final MmCoords coords;

      Image(short[] pix, int w, int h, MmCoords coords) {
         this.pix = pix;
         this.w = w;
         this.h = h;
         this.coords = coords;
      }

      public Object getRawPixels() {
         return pix;
      }

      public int getWidth() {
         return w;
      }

      public int getHeight() {
         return h;
      }

      public int getBytesPerPixel() {
         return 2;
      }

      public int getNumComponents() {
         return 1;
      }

      public MmCoords getCoords() {
         return coords;
      }

      public String getMetadataJson() {
         return "{\"Camera\":\"Test\"}";
      }
   }

   @Test
   void adapterRoundTrip(@TempDir Path dir) {
      OMEBigTiffStorageConfig cfg = new OMEBigTiffStorageConfig()
            .numResolutionLevels(2)
            .axes(Arrays.asList(
                  AxisInfo.builder("channel").type(DimensionType.CHANNEL).build(),
                  AxisInfo.builder("z").type(DimensionType.SPACE).build()));
      MmStorageAdapterExample adapter =
            new MmStorageAdapterExample(dir.toString(), "mmacq", cfg);
      adapter.setSummaryMetadata("{\"Prefix\":\"mmacq\"}");

      int w = 48;
      int h = 32;
      short[] pix = new short[w * h];
      for (int i = 0; i < pix.length; i++) {
         pix[i] = (short) (i * 3);
      }
      // channel 1, z 2; time omitted (defaults to 0), mirroring MM's zero-omission.
      MmCoords coords = new Coords().set("channel", 1).set("z", 2);
      adapter.putImage(new Image(pix, w, h, coords));
      assertTrue(adapter.hasImage(coords));
      adapter.finish();

      OMEBigTiffImage img = adapter.getImage(coords);
      assertNotNull(img);
      assertArrayEquals(pix, (short[]) img.pix);
      adapter.close();
   }
}
