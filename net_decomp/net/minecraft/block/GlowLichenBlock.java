/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.function.ToIntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.MultifaceGrower;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class GlowLichenBlock
extends MultifaceGrowthBlock
implements Fertilizable {
    public static final MapCodec<GlowLichenBlock> CODEC = GlowLichenBlock.createCodec(GlowLichenBlock::new);
    private final MultifaceGrower grower = new MultifaceGrower(this);

    public MapCodec<GlowLichenBlock> getCodec() {
        return CODEC;
    }

    public GlowLichenBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public static ToIntFunction<BlockState> getLuminanceSupplier(int luminance) {
        return state -> MultifaceBlock.hasAnyDirection(state) ? luminance : 0;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return Direction.stream().anyMatch(direction -> this.grower.canGrow(state, world, pos, direction.getOpposite()));
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.grower.grow(state, (WorldAccess)world, pos, random);
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    @Override
    public MultifaceGrower getGrower() {
        return this.grower;
    }
}

