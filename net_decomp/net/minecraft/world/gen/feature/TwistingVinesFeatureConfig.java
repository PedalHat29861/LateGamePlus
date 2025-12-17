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
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.feature.FeatureConfig;

public record TwistingVinesFeatureConfig(int spreadWidth, int spreadHeight, int maxHeight) implements FeatureConfig
{
    public static final Codec<TwistingVinesFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.POSITIVE_INT.fieldOf("spread_width").forGetter(TwistingVinesFeatureConfig::spreadWidth), (App)Codecs.POSITIVE_INT.fieldOf("spread_height").forGetter(TwistingVinesFeatureConfig::spreadHeight), (App)Codecs.POSITIVE_INT.fieldOf("max_height").forGetter(TwistingVinesFeatureConfig::maxHeight)).apply((Applicative)instance, TwistingVinesFeatureConfig::new));
}

