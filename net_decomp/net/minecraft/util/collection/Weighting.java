/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.collection;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

public class Weighting {
    private Weighting() {
    }

    public static <T> int getWeightSum(List<T> pool, ToIntFunction<T> weightGetter) {
        long l = 0L;
        for (T object : pool) {
            l += (long)weightGetter.applyAsInt(object);
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T> Optional<T> getRandom(Random random, List<T> pool, int totalWeight, ToIntFunction<T> weightGetter) {
        if (totalWeight < 0) {
            throw Util.getFatalOrPause(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (totalWeight == 0) {
            return Optional.empty();
        }
        int i = random.nextInt(totalWeight);
        return Weighting.getAt(pool, i, weightGetter);
    }

    public static <T> Optional<T> getAt(List<T> pool, int totalWeight, ToIntFunction<T> weightGetter) {
        for (T object : pool) {
            if ((totalWeight -= weightGetter.applyAsInt(object)) >= 0) continue;
            return Optional.of(object);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getRandom(Random random, List<T> pool, ToIntFunction<T> weightGetter) {
        return Weighting.getRandom(random, pool, Weighting.getWeightSum(pool, weightGetter), weightGetter);
    }
}

