package org.micromanager.mmomebigtiff.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-image metadata stored as an append-only NDJSON sidecar
 * ({@code ome-metadata.ndjson}): one line {@code {"axes":{...},"meta":<json>}} per image.
 *
 * <p>An in-memory {@code axesKey -> metadataJson} map is the source of truth for reads and is
 * updated synchronously (via {@link #put}) as images are recorded, so metadata is readable the
 * instant an image is queued (mirroring NDTiffStorage's separate index sidecar). The file
 * append ({@link #append}) is done separately — on the storage's writer thread — so JSON
 * validation and the flushed disk write never add latency to the acquisition thread. The file
 * is append-only, which keeps it crash-tolerant and safe to tail while writing.
 *
 * <p>Per-image metadata is also embedded in each plane's TIFF {@code ImageDescription}-sibling
 * private tag by the writer, so a standalone TIFF file is self-describing; the sidecar keeps the
 * fast, appendable index used for read-while-write.
 */
public final class PerImageMetadataStore implements AutoCloseable {

   private static final ObjectMapper MAPPER = new ObjectMapper();
   private static final TypeReference<Map<String, Object>> MAP_TYPE =
         new TypeReference<Map<String, Object>>() { };
   private static final String FILE_NAME = "ome-metadata.ndjson";

   private final Path file;
   private final Map<String, String> byKey = new ConcurrentHashMap<>();
   private BufferedWriter writer; // null in read-only mode

   private PerImageMetadataStore(Path file, boolean writable) {
      this.file = file;
      if (writable) {
         try {
            this.writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                  StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                  StandardOpenOption.APPEND);
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }
   }

   /** Open for writing (creates/appends the sidecar). */
   public static PerImageMetadataStore createWritable(Path datasetRoot) {
      return new PerImageMetadataStore(datasetRoot.resolve(FILE_NAME), true);
   }

   /** Open for reading; loads any existing sidecar into the in-memory index. */
   public static PerImageMetadataStore openReadOnly(Path datasetRoot) {
      PerImageMetadataStore s = new PerImageMetadataStore(datasetRoot.resolve(FILE_NAME), false);
      s.load();
      return s;
   }

   /**
    * Record per-image metadata in the in-memory index (readable immediately). Call
    * {@link #append} afterwards — typically from the writer thread — to persist it.
    *
    * @param axesKey      canonical serialized axes key
    * @param metadataJson per-image metadata JSON (may be null)
    */
   public void put(String axesKey, String metadataJson) {
      byKey.put(axesKey, metadataJson == null ? "null" : metadataJson);
   }

   /**
    * Append one NDJSON line to the sidecar. Safe to call from a single writer thread while
    * readers use {@link #get}; no-op in read-only mode.
    *
    * @param axes         the axes map (stored for round-trip)
    * @param metadataJson per-image metadata JSON (may be null)
    */
   public synchronized void append(Map<String, Object> axes, String metadataJson) {
      if (writer == null) {
         return;
      }
      try {
         Map<String, Object> line = new java.util.LinkedHashMap<>();
         line.put("axes", axes);
         line.put("meta", metadataJson == null ? null : MAPPER.readTree(metadataJson));
         writer.write(MAPPER.writeValueAsString(line));
         writer.newLine();
         writer.flush();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /** Metadata JSON for the given key, or null if none recorded. */
   public String get(String axesKey) {
      String v = byKey.get(axesKey);
      return "null".equals(v) ? null : v;
   }

   public boolean has(String axesKey) {
      return byKey.containsKey(axesKey);
   }

   /** Unmodifiable view of every recorded axes key (serialized form). */
   public Set<String> keySet() {
      return Collections.unmodifiableSet(byKey.keySet());
   }

   private void load() {
      if (!Files.exists(file)) {
         return;
      }
      try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
         String raw;
         while ((raw = reader.readLine()) != null) {
            if (raw.trim().isEmpty()) {
               continue;
            }
            Map<String, Object> obj = MAPPER.readValue(raw, MAP_TYPE);
            @SuppressWarnings("unchecked")
            Map<String, Object> axes = (Map<String, Object>) obj.get("axes");
            Object meta = obj.get("meta");
            String key = org.micromanager.mmomebigtiff.AxesKey.serialize(axes);
            byKey.put(key, meta == null ? "null" : MAPPER.writeValueAsString(meta));
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public synchronized void close() {
      if (writer != null) {
         try {
            writer.close();
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
         writer = null;
      }
   }
}
