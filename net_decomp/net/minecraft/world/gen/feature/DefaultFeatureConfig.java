/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DefaultFeatureConfig
implements FeatureConfig {
    public static final DefaultFeatureConfig INSTANCE = new DefaultFeatureConfig();
    public static final Codec<DefaultFeatureConfig> CODEC = MapCodec.unitCodec((Object)INSTANCE);
}

