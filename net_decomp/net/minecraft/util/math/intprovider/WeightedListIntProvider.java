/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public class WeightedListIntProvider
extends IntProvider {
    public static final MapCodec<WeightedListIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Pool.createNonEmptyCodec(IntProvider.VALUE_CODEC).fieldOf("distribution").forGetter(provider -> provider.weightedList)).apply((Applicative)instance, WeightedListIntProvider::new));
    private final Pool<IntProvider> weightedList;
    private final int min;
    private final int max;

    public WeightedListIntProvider(Pool<IntProvider> weightedList) {
        this.weightedList = weightedList;
        int i = Integer.MAX_VALUE;
        int j = Integer.MIN_VALUE;
        for (Weighted<IntProvider> weighted : weightedList.getEntries()) {
            int k = weighted.value().getMin();
            int l = weighted.value().getMax();
            i = Math.min(i, k);
            j = Math.max(j, l);
        }
        this.min = i;
        this.max = j;
    }

    @Override
    public int get(Random random) {
        return this.weightedList.get(random).get(random);
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.WEIGHTED_LIST;
    }
}

