/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class SimpleNeighborUpdater
implements NeighborUpdater {
    private final World world;

    public SimpleNeighborUpdater(World world) {
        this.world = world;
    }

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
        NeighborUpdater.replaceWithStateForNeighborUpdate(this.world, direction, pos, neighborPos, neighborState, flags, maxUpdateDepth - 1);
    }

    @Override
    public void updateNeighbor(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation) {
        BlockState blockState = this.world.getBlockState(pos);
        this.updateNeighbor(blockState, pos, sourceBlock, orientation, false);
    }

    @Override
    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation, boolean notify) {
        NeighborUpdater.tryNeighborUpdate(this.world, state, pos, sourceBlock, orientation, notify);
    }
}

