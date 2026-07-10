package org.micromanager.mmomebigtiff.tiff;

import org.micromanager.mmomebigtiff.PixelType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Converts primitive pixel arrays ({@code byte[]}/{@code short[]}/{@code float[]}) to and from
 * the raw byte representation stored in a TIFF strip, applying the file's byte order and, when
 * requested, Deflate (zlib) compression (TIFF compression code 8).
 */
public final class TiffPixelCodec {

   private TiffPixelCodec() { }

   /** Raw (uncompressed) byte length of a plane of {@code numPixels} pixels of the given type. */
   public static int rawByteCount(PixelType type, int numPixels) {
      return numPixels * type.bytesPerPixel();
   }

   /** Encode a primitive pixel array to raw little/big-endian bytes (no compression). */
   public static byte[] toRawBytes(Object pixels, PixelType type, ByteOrder order) {
      switch (type) {
         case GRAY8: {
            byte[] p = (byte[]) pixels;
            return p.clone();
         }
         case GRAY16: {
            short[] p = (short[]) pixels;
            ByteBuffer bb = ByteBuffer.allocate(p.length * 2).order(order);
            bb.asShortBuffer().put(p);
            return bb.array();
         }
         case GRAY32: {
            float[] p = (float[]) pixels;
            ByteBuffer bb = ByteBuffer.allocate(p.length * 4).order(order);
            bb.asFloatBuffer().put(p);
            return bb.array();
         }
         default:
            throw new IllegalArgumentException("Unsupported pixel type: " + type);
      }
   }

   /** Decode raw bytes (matching {@link #toRawBytes}) back to a primitive pixel array. */
   public static Object fromRawBytes(byte[] raw, PixelType type, ByteOrder order, int numPixels) {
      switch (type) {
         case GRAY8: {
            if (raw.length == numPixels) {
               return raw;
            }
            byte[] out = new byte[numPixels];
            System.arraycopy(raw, 0, out, 0, Math.min(numPixels, raw.length));
            return out;
         }
         case GRAY16: {
            short[] out = new short[numPixels];
            ByteBuffer.wrap(raw).order(order).asShortBuffer().get(out);
            return out;
         }
         case GRAY32: {
            float[] out = new float[numPixels];
            ByteBuffer.wrap(raw).order(order).asFloatBuffer().get(out);
            return out;
         }
         default:
            throw new IllegalArgumentException("Unsupported pixel type: " + type);
      }
   }

   /** Deflate (zlib) compression of a raw strip. */
   public static byte[] deflate(byte[] raw) {
      Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
      deflater.setInput(raw);
      deflater.finish();
      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(64, raw.length / 2));
      byte[] buf = new byte[8192];
      while (!deflater.finished()) {
         int n = deflater.deflate(buf);
         out.write(buf, 0, n);
      }
      deflater.end();
      return out.toByteArray();
   }

   /** Inverse of {@link #deflate}; {@code rawLength} is the known uncompressed size. */
   public static byte[] inflate(byte[] compressed, int rawLength) {
      Inflater inflater = new Inflater();
      inflater.setInput(compressed);
      byte[] out = new byte[rawLength];
      try {
         int total = 0;
         while (total < rawLength && !inflater.finished()) {
            int n = inflater.inflate(out, total, rawLength - total);
            if (n == 0 && inflater.needsInput()) {
               break;
            }
            total += n;
         }
      } catch (DataFormatException e) {
         throw new IllegalStateException("Corrupt Deflate strip", e);
      } finally {
         inflater.end();
      }
      return out;
   }
}
