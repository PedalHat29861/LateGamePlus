/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;

public record DiskFeatureConfig(PredicatedStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) implements FeatureConfig
{
    public static final Codec<DiskFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(DiskFeatureConfig::stateProvider), (App)BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(DiskFeatureConfig::target), (App)IntProvider.createValidatingCodec(0, 8).fieldOf("radius").forGetter(DiskFeatureConfig::radius), (App)Codec.intRange((int)0, (int)4).fieldOf("half_height").forGetter(DiskFeatureConfig::halfHeight)).apply((Applicative)instance, DiskFeatureConfig::new));
}

