/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jspecify.annotations.Nullable;

public final class EmptyBlockView
extends Enum<EmptyBlockView>
implements BlockView {
    public static final /* enum */ EmptyBlockView INSTANCE = new EmptyBlockView();
    private static final /* synthetic */ EmptyBlockView[] field_12295;

    public static EmptyBlockView[] values() {
        return (EmptyBlockView[])field_12295.clone();
    }

    public static EmptyBlockView valueOf(String string) {
        return Enum.valueOf(EmptyBlockView.class, string);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }

    @Override
    public int getBottomY() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    private static /* synthetic */ EmptyBlockView[] method_36692() {
        return new EmptyBlockView[]{INSTANCE};
    }

    static {
        field_12295 = EmptyBlockView.method_36692();
    }
}

