package org.micromanager.mmomebigtiff;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a fixture dataset to {@code target/verify-fixture.ome.tiff} that {@code verify_tiff.py}
 * reads back with the independent {@code tifffile} stack to confirm cross-language conformance
 * (valid pyramidal OME-BigTIFF: BigTIFF header, SubIFD pyramid, OME-XML, exact pixels).
 */
public class VerificationFixtureTest {

   @Test
   void writeFixture() throws Exception {
      Path target = Paths.get("target");
      Files.createDirectories(target);
      Path fixture = target.resolve("verify-fixture.ome.tiff");
      // Clean any previous run.
      if (Files.exists(fixture)) {
         try (java.util.stream.Stream<Path> s = Files.walk(fixture)) {
            s.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                  .forEach(p -> p.toFile().delete());
         }
      }

      OMEBigTiffStorageConfig cfg = new OMEBigTiffStorageConfig()
            .numResolutionLevels(3)
            .pixelSize(0.325)
            .spatialUnit("micrometer")
            .addAxis(AxisInfo.builder("time").type(DimensionType.TIME).build())
            .addAxis(AxisInfo.builder("channel").type(DimensionType.CHANNEL)
                  .channels(Arrays.asList(
                        Channel.builder("DAPI").color("#0000FF").emissionWavelengthNm(461).build(),
                        Channel.builder("FITC").color("#00FF00").emissionWavelengthNm(519).build()))
                  .build())
            .addAxis(AxisInfo.builder("z").type(DimensionType.SPACE).build());

      OMEBigTiffStorage store = new OMEBigTiffStorage("target", "verify-fixture",
            "{\"Acquisition\":\"fixture\"}", cfg);

      int w = 96;
      int h = 64;
      int sizeT = 2;
      int sizeC = 2;
      int sizeZ = 3;
      for (int t = 0; t < sizeT; t++) {
         for (int c = 0; c < sizeC; c++) {
            for (int z = 0; z < sizeZ; z++) {
               short[] pix = new short[w * h];
               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     // Deterministic, dimension-dependent pattern for cross-checking.
                     pix[y * w + x] = (short) ((x + y + 100 * t + 10 * c + z) & 0xFFFF);
                  }
               }
               Map<String, Object> axes = new HashMap<>();
               axes.put("time", t);
               axes.put("channel", c);
               axes.put("z", z);
               store.putImage(pix, "{\"t\":" + t + ",\"c\":" + c + ",\"z\":" + z + "}",
                     axes, false, 16, h, w);
            }
         }
      }
      store.setCustomMetadata("note", "{\"hello\":\"world\"}");
      store.finishedWriting();
      store.close();

      System.out.println("Wrote fixture to " + fixture.toAbsolutePath());
   }
}
