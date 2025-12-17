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
import net.minecraft.world.gen.feature.GeodeFeatureConfig;

public class GeodeCrackConfig {
    public static final Codec<GeodeCrackConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GeodeFeatureConfig.RANGE.fieldOf("generate_crack_chance").orElse((Object)1.0).forGetter(config -> config.generateCrackChance), (App)Codec.doubleRange((double)0.0, (double)5.0).fieldOf("base_crack_size").orElse((Object)2.0).forGetter(config -> config.baseCrackSize), (App)Codec.intRange((int)0, (int)10).fieldOf("crack_point_offset").orElse((Object)2).forGetter(config -> config.crackPointOffset)).apply((Applicative)instance, GeodeCrackConfig::new));
    public final double generateCrackChance;
    public final double baseCrackSize;
    public final int crackPointOffset;

    public GeodeCrackConfig(double generateCrackChance, double baseCrackSize, int crackPointOffset) {
        this.generateCrackChance = generateCrackChance;
        this.baseCrackSize = baseCrackSize;
        this.crackPointOffset = crackPointOffset;
    }
}

