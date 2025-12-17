/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.light.LightSourceView;
import org.jspecify.annotations.Nullable;

public interface ChunkProvider {
    public @Nullable LightSourceView getChunk(int var1, int var2);

    default public void onLightUpdate(LightType type, ChunkSectionPos pos) {
    }

    public BlockView getWorld();
}

