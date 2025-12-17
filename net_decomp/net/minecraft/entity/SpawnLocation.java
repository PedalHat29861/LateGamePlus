/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jspecify.annotations.Nullable;

public interface SpawnLocation {
    public boolean isSpawnPositionOk(WorldView var1, BlockPos var2, @Nullable EntityType<?> var3);

    default public BlockPos adjustPosition(WorldView world, BlockPos pos) {
        return pos;
    }
}

