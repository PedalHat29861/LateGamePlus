/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public abstract class AbstractBannerBlock
extends BlockWithEntity {
    private final DyeColor color;

    protected AbstractBannerBlock(DyeColor color, AbstractBlock.Settings settings) {
        super(settings);
        this.color = color;
    }

    protected abstract MapCodec<? extends AbstractBannerBlock> getCodec();

    @Override
    public boolean canMobSpawnInside(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BannerBlockEntity(pos, state, this.color);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BannerBlockEntity) {
            BannerBlockEntity bannerBlockEntity = (BannerBlockEntity)blockEntity;
            return bannerBlockEntity.getPickStack();
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    public DyeColor getColor() {
        return this.color;
    }
}

