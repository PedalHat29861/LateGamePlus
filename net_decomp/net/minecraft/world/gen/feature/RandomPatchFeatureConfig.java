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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;

public record RandomPatchFeatureConfig(int tries, int xzSpread, int ySpread, RegistryEntry<PlacedFeature> feature) implements FeatureConfig
{
    public static final Codec<RandomPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.POSITIVE_INT.fieldOf("tries").orElse((Object)128).forGetter(RandomPatchFeatureConfig::tries), (App)Codecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse((Object)7).forGetter(RandomPatchFeatureConfig::xzSpread), (App)Codecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse((Object)3).forGetter(RandomPatchFeatureConfig::ySpread), (App)PlacedFeature.REGISTRY_CODEC.fieldOf("feature").forGetter(RandomPatchFeatureConfig::feature)).apply((Applicative)instance, RandomPatchFeatureConfig::new));
}

