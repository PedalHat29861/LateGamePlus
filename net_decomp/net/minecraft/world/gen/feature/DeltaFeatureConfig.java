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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class DeltaFeatureConfig
implements FeatureConfig {
    public static final Codec<DeltaFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockState.CODEC.fieldOf("contents").forGetter(config -> config.contents), (App)BlockState.CODEC.fieldOf("rim").forGetter(config -> config.rim), (App)IntProvider.createValidatingCodec(0, 16).fieldOf("size").forGetter(config -> config.size), (App)IntProvider.createValidatingCodec(0, 16).fieldOf("rim_size").forGetter(config -> config.rimSize)).apply((Applicative)instance, DeltaFeatureConfig::new));
    private final BlockState contents;
    private final BlockState rim;
    private final IntProvider size;
    private final IntProvider rimSize;

    public DeltaFeatureConfig(BlockState contents, BlockState rim, IntProvider size, IntProvider rimSize) {
        this.contents = contents;
        this.rim = rim;
        this.size = size;
        this.rimSize = rimSize;
    }

    public BlockState getContents() {
        return this.contents;
    }

    public BlockState getRim() {
        return this.rim;
    }

    public IntProvider getSize() {
        return this.size;
    }

    public IntProvider getRimSize() {
        return this.rimSize;
    }
}

