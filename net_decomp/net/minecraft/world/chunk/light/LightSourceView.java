/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk.light;

import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.light.ChunkSkyLight;

public interface LightSourceView
extends BlockView {
    public void forEachLightSource(BiConsumer<BlockPos, BlockState> var1);

    public ChunkSkyLight getChunkSkyLight();
}

