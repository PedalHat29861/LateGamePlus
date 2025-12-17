/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.encoding;

import io.netty.buffer.ByteBuf;

public class VarLongs {
    private static final int MAX_BYTES = 10;
    private static final int DATA_BITS_MASK = 127;
    private static final int MORE_BITS_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getSizeInBytes(long l) {
        for (int i = 1; i < 10; ++i) {
            if ((l & -1L << i * 7) != 0L) continue;
            return i;
        }
        return 10;
    }

    public static boolean shouldContinueRead(byte b) {
        return (b & 0x80) == 128;
    }

    public static long read(ByteBuf buf) {
        byte b;
        long l = 0L;
        int i = 0;
        do {
            b = buf.readByte();
            l |= (long)(b & 0x7F) << i++ * 7;
            if (i <= 10) continue;
            throw new RuntimeException("VarLong too big");
        } while (VarLongs.shouldContinueRead(b));
        return l;
    }

    public static ByteBuf write(ByteBuf buf, long l) {
        while (true) {
            if ((l & 0xFFFFFFFFFFFFFF80L) == 0L) {
                buf.writeByte((int)l);
                return buf;
            }
            buf.writeByte((int)(l & 0x7FL) | 0x80);
            l >>>= 7;
        }
    }
}

