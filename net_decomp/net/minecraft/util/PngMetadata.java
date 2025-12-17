/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HexFormat;

public record PngMetadata(int width, int height) {
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase().withPrefix("0x");
    private static final long PNG_SIGNATURE = -8552249625308161526L;
    private static final int IHDR_CHUNK_TYPE = 1229472850;
    private static final int IHDR_CHUNK_LENGTH = 13;

    public static PngMetadata fromStream(InputStream stream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(stream);
        long l = dataInputStream.readLong();
        if (l != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature: " + HEX_FORMAT.toHexDigits(l));
        }
        int i = dataInputStream.readInt();
        if (i != 13) {
            throw new IOException("Bad length for IHDR chunk: " + i);
        }
        int j = dataInputStream.readInt();
        if (j != 1229472850) {
            throw new IOException("Bad type for IHDR chunk: " + HEX_FORMAT.toHexDigits(j));
        }
        int k = dataInputStream.readInt();
        int m = dataInputStream.readInt();
        return new PngMetadata(k, m);
    }

    public static PngMetadata fromBytes(byte[] bytes) throws IOException {
        return PngMetadata.fromStream(new ByteArrayInputStream(bytes));
    }

    public static void validate(ByteBuffer buf) throws IOException {
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.BIG_ENDIAN);
        if (buf.limit() < 16) {
            throw new IOException("PNG header missing");
        }
        if (buf.getLong(0) != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        }
        if (buf.getInt(8) != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        }
        if (buf.getInt(12) != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        }
        buf.order(byteOrder);
    }
}

