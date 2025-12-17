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
import net.minecraft.world.gen.feature.FeatureConfig;

public record SculkPatchFeatureConfig(int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance) implements FeatureConfig
{
    public static final Codec<SculkPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)1, (int)32).fieldOf("charge_count").forGetter(SculkPatchFeatureConfig::chargeCount), (App)Codec.intRange((int)1, (int)500).fieldOf("amount_per_charge").forGetter(SculkPatchFeatureConfig::amountPerCharge), (App)Codec.intRange((int)1, (int)64).fieldOf("spread_attempts").forGetter(SculkPatchFeatureConfig::spreadAttempts), (App)Codec.intRange((int)0, (int)8).fieldOf("growth_rounds").forGetter(SculkPatchFeatureConfig::growthRounds), (App)Codec.intRange((int)0, (int)8).fieldOf("spread_rounds").forGetter(SculkPatchFeatureConfig::spreadRounds), (App)IntProvider.VALUE_CODEC.fieldOf("extra_rare_growths").forGetter(SculkPatchFeatureConfig::extraRareGrowths), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("catalyst_chance").forGetter(SculkPatchFeatureConfig::catalystChance)).apply((Applicative)instance, SculkPatchFeatureConfig::new));
}

