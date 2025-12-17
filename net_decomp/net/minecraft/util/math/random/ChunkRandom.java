/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math.random;

import java.util.function.LongFunction;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class ChunkRandom
extends CheckedRandom {
    private final Random baseRandom;
    private int sampleCount;

    public ChunkRandom(Random baseRandom) {
        super(0L);
        this.baseRandom = baseRandom;
    }

    public int getSampleCount() {
        return this.sampleCount;
    }

    @Override
    public Random split() {
        return this.baseRandom.split();
    }

    @Override
    public RandomSplitter nextSplitter() {
        return this.baseRandom.nextSplitter();
    }

    @Override
    public int next(int bits) {
        ++this.sampleCount;
        Random random = this.baseRandom;
        if (random instanceof CheckedRandom) {
            CheckedRandom checkedRandom = (CheckedRandom)random;
            return checkedRandom.next(bits);
        }
        return (int)(this.baseRandom.nextLong() >>> 64 - bits);
    }

    @Override
    public synchronized void setSeed(long seed) {
        if (this.baseRandom == null) {
            return;
        }
        this.baseRandom.setSeed(seed);
    }

    public long setPopulationSeed(long worldSeed, int blockX, int blockZ) {
        this.setSeed(worldSeed);
        long l = this.nextLong() | 1L;
        long m = this.nextLong() | 1L;
        long n = (long)blockX * l + (long)blockZ * m ^ worldSeed;
        this.setSeed(n);
        return n;
    }

    public void setDecoratorSeed(long populationSeed, int index, int step) {
        long l = populationSeed + (long)index + (long)(10000 * step);
        this.setSeed(l);
    }

    public void setCarverSeed(long worldSeed, int chunkX, int chunkZ) {
        this.setSeed(worldSeed);
        long l = this.nextLong();
        long m = this.nextLong();
        long n = (long)chunkX * l ^ (long)chunkZ * m ^ worldSeed;
        this.setSeed(n);
    }

    public void setRegionSeed(long worldSeed, int regionX, int regionZ, int salt) {
        long l = (long)regionX * 341873128712L + (long)regionZ * 132897987541L + worldSeed + (long)salt;
        this.setSeed(l);
    }

    public static Random getSlimeRandom(int chunkX, int chunkZ, long worldSeed, long scrambler) {
        return Random.create(worldSeed + (long)(chunkX * chunkX * 4987142) + (long)(chunkX * 5947611) + (long)(chunkZ * chunkZ) * 4392871L + (long)(chunkZ * 389711) ^ scrambler);
    }

    public static final class RandomProvider
    extends Enum<RandomProvider> {
        public static final /* enum */ RandomProvider LEGACY = new RandomProvider(CheckedRandom::new);
        public static final /* enum */ RandomProvider XOROSHIRO = new RandomProvider(Xoroshiro128PlusPlusRandom::new);
        private final LongFunction<Random> provider;
        private static final /* synthetic */ RandomProvider[] field_35145;

        public static RandomProvider[] values() {
            return (RandomProvider[])field_35145.clone();
        }

        public static RandomProvider valueOf(String string) {
            return Enum.valueOf(RandomProvider.class, string);
        }

        private RandomProvider(LongFunction<Random> provider) {
            this.provider = provider;
        }

        public Random create(long seed) {
            return this.provider.apply(seed);
        }

        private static /* synthetic */ RandomProvider[] method_39005() {
            return new RandomProvider[]{LEGACY, XOROSHIRO};
        }

        static {
            field_35145 = RandomProvider.method_39005();
        }
    }
}

