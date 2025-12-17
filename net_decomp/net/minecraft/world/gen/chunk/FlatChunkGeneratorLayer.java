/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.world.dimension.DimensionType;

public class FlatChunkGeneratorLayer {
    public static final Codec<FlatChunkGeneratorLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)DimensionType.MAX_HEIGHT).fieldOf("height").forGetter(FlatChunkGeneratorLayer::getThickness), (App)Registries.BLOCK.getCodec().fieldOf("block").orElse((Object)Blocks.AIR).forGetter(layer -> layer.getBlockState().getBlock())).apply((Applicative)instance, FlatChunkGeneratorLayer::new));
    private final Block block;
    private final int thickness;

    public FlatChunkGeneratorLayer(int thickness, Block block) {
        this.thickness = thickness;
        this.block = block;
    }

    public int getThickness() {
        return this.thickness;
    }

    public BlockState getBlockState() {
        return this.block.getDefaultState();
    }

    public FlatChunkGeneratorLayer withMaxThickness(int maxThickness) {
        if (this.thickness > maxThickness) {
            return new FlatChunkGeneratorLayer(maxThickness, this.block);
        }
        return this;
    }

    public String toString() {
        return (String)(this.thickness != 1 ? this.thickness + "*" : "") + String.valueOf(Registries.BLOCK.getId(this.block));
    }
}

