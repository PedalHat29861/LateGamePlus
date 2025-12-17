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
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jspecify.annotations.Nullable;

public final class EmptyBlockRenderView
extends Enum<EmptyBlockRenderView>
implements BlockRenderView {
    public static final /* enum */ EmptyBlockRenderView INSTANCE = new EmptyBlockRenderView();
    private static final /* synthetic */ EmptyBlockRenderView[] field_52612;

    public static EmptyBlockRenderView[] values() {
        return (EmptyBlockRenderView[])field_52612.clone();
    }

    public static EmptyBlockRenderView valueOf(String string) {
        return Enum.valueOf(EmptyBlockRenderView.class, string);
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 1.0f;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return LightingProvider.DEFAULT;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return -1;
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
    public int getHeight() {
        return 0;
    }

    @Override
    public int getBottomY() {
        return 0;
    }

    private static /* synthetic */ EmptyBlockRenderView[] method_61721() {
        return new EmptyBlockRenderView[]{INSTANCE};
    }

    static {
        field_52612 = EmptyBlockRenderView.method_61721();
    }
}

