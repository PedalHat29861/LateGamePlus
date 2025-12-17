/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

public interface ModifiableWorld {
    public boolean setBlockState(BlockPos var1, BlockState var2, @Block.SetBlockStateFlag int var3, int var4);

    default public boolean setBlockState(BlockPos pos, BlockState state, @Block.SetBlockStateFlag int flags) {
        return this.setBlockState(pos, state, flags, 512);
    }

    public boolean removeBlock(BlockPos var1, boolean var2);

    default public boolean breakBlock(BlockPos pos, boolean drop) {
        return this.breakBlock(pos, drop, null);
    }

    default public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity) {
        return this.breakBlock(pos, drop, breakingEntity, 512);
    }

    public boolean breakBlock(BlockPos var1, boolean var2, @Nullable Entity var3, int var4);

    default public boolean spawnEntity(Entity entity) {
        return false;
    }
}

