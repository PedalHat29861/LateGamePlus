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
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record SimpleBlockFeatureConfig(BlockStateProvider toPlace, boolean scheduleTick) implements FeatureConfig
{
    public static final Codec<SimpleBlockFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.TYPE_CODEC.fieldOf("to_place").forGetter(config -> config.toPlace), (App)Codec.BOOL.optionalFieldOf("schedule_tick", (Object)false).forGetter(config -> config.scheduleTick)).apply((Applicative)instance, SimpleBlockFeatureConfig::new));

    public SimpleBlockFeatureConfig(BlockStateProvider toPlace) {
        this(toPlace, false);
    }
}

