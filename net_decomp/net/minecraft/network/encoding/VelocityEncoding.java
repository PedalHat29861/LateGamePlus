/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.encoding;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VelocityEncoding {
    private static final int field_62269 = 15;
    private static final int MAX_15_BIT_INT = Short.MAX_VALUE;
    private static final double field_62270 = 32766.0;
    private static final int field_62271 = 2;
    private static final int SLOW_BIT_MASK = 3;
    private static final int field_62272 = 4;
    private static final int field_62273 = 3;
    private static final int field_62274 = 18;
    private static final int field_62275 = 33;
    public static final double field_62267 = 1.7179869183E10;
    public static final double field_62268 = 3.051944088384301E-5;

    public static boolean hasFastMarkerBit(int maxDirectionalVelocity) {
        return (maxDirectionalVelocity & 4) == 4;
    }

    public static Vec3d readVelocity(ByteBuf buf) {
        short i = buf.readUnsignedByte();
        if (i == 0) {
            return Vec3d.ZERO;
        }
        short j = buf.readUnsignedByte();
        long l = buf.readUnsignedInt();
        long m = l << 16 | (long)(j << 8) | (long)i;
        long n = i & 3;
        if (VelocityEncoding.hasFastMarkerBit(i)) {
            n |= ((long)VarInts.read(buf) & 0xFFFFFFFFL) << 2;
        }
        return new Vec3d(VelocityEncoding.fromLong(m >> 3) * (double)n, VelocityEncoding.fromLong(m >> 18) * (double)n, VelocityEncoding.fromLong(m >> 33) * (double)n);
    }

    public static void writeVelocity(ByteBuf buf, Vec3d velocity) {
        double f;
        double e;
        double d = VelocityEncoding.clampValue(velocity.x);
        double g = MathHelper.absMax(d, MathHelper.absMax(e = VelocityEncoding.clampValue(velocity.y), f = VelocityEncoding.clampValue(velocity.z)));
        if (g < 3.051944088384301E-5) {
            buf.writeByte(0);
            return;
        }
        long l = MathHelper.ceilLong(g);
        boolean bl = (l & 3L) != l;
        long m = bl ? l & 3L | 4L : l;
        long n = VelocityEncoding.toLong(d / (double)l) << 3;
        long o = VelocityEncoding.toLong(e / (double)l) << 18;
        long p = VelocityEncoding.toLong(f / (double)l) << 33;
        long q = m | n | o | p;
        buf.writeByte((int)((byte)q));
        buf.writeByte((int)((byte)(q >> 8)));
        buf.writeInt((int)(q >> 16));
        if (bl) {
            VarInts.write(buf, (int)(l >> 2));
        }
    }

    private static double clampValue(double value) {
        return Double.isNaN(value) ? 0.0 : Math.clamp(value, -1.7179869183E10, 1.7179869183E10);
    }

    private static long toLong(double value) {
        return Math.round((value * 0.5 + 0.5) * 32766.0);
    }

    private static double fromLong(long value) {
        return Math.min((double)(value & 0x7FFFL), 32766.0) * 2.0 / 32766.0 - 1.0;
    }
}

