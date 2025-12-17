/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hashing
 *  com.google.common.primitives.Longs
 */
package net.minecraft.util.math.random;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSeed {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final HashFunction MD5_HASH = Hashing.md5();
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    @VisibleForTesting
    public static long mixStafford13(long seed) {
        seed = (seed ^ seed >>> 30) * -4658895280553007687L;
        seed = (seed ^ seed >>> 27) * -7723592293110705685L;
        return seed ^ seed >>> 31;
    }

    public static XoroshiroSeed createUnmixedXoroshiroSeed(long seed) {
        long l = seed ^ 0x6A09E667F3BCC909L;
        long m = l + -7046029254386353131L;
        return new XoroshiroSeed(l, m);
    }

    public static XoroshiroSeed createXoroshiroSeed(long seed) {
        return RandomSeed.createUnmixedXoroshiroSeed(seed).mix();
    }

    public static XoroshiroSeed createXoroshiroSeed(String seed) {
        byte[] bs = MD5_HASH.hashString((CharSequence)seed, StandardCharsets.UTF_8).asBytes();
        long l = Longs.fromBytes((byte)bs[0], (byte)bs[1], (byte)bs[2], (byte)bs[3], (byte)bs[4], (byte)bs[5], (byte)bs[6], (byte)bs[7]);
        long m = Longs.fromBytes((byte)bs[8], (byte)bs[9], (byte)bs[10], (byte)bs[11], (byte)bs[12], (byte)bs[13], (byte)bs[14], (byte)bs[15]);
        return new XoroshiroSeed(l, m);
    }

    public static long getSeed() {
        return SEED_UNIQUIFIER.updateAndGet(seedUniquifier -> seedUniquifier * 1181783497276652981L) ^ System.nanoTime();
    }

    public record XoroshiroSeed(long seedLo, long seedHi) {
        public XoroshiroSeed split(long seedLo, long seedHi) {
            return new XoroshiroSeed(this.seedLo ^ seedLo, this.seedHi ^ seedHi);
        }

        public XoroshiroSeed split(XoroshiroSeed seed) {
            return this.split(seed.seedLo, seed.seedHi);
        }

        public XoroshiroSeed mix() {
            return new XoroshiroSeed(RandomSeed.mixStafford13(this.seedLo), RandomSeed.mixStafford13(this.seedHi));
        }
    }
}

