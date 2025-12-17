/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.heightprovider;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProviderType;

public class WeightedListHeightProvider
extends HeightProvider {
    public static final MapCodec<WeightedListHeightProvider> WEIGHTED_LIST_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Pool.createNonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter(weightedListHeightProvider -> weightedListHeightProvider.weightedList)).apply((Applicative)instance, WeightedListHeightProvider::new));
    private final Pool<HeightProvider> weightedList;

    public WeightedListHeightProvider(Pool<HeightProvider> weightedList) {
        this.weightedList = weightedList;
    }

    @Override
    public int get(Random random, HeightContext context) {
        return this.weightedList.get(random).get(random, context);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}

