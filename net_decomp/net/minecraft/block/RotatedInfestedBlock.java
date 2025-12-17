/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class RotatedInfestedBlock
extends InfestedBlock {
    public static final MapCodec<RotatedInfestedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.BLOCK.getCodec().fieldOf("host").forGetter(InfestedBlock::getRegularBlock), RotatedInfestedBlock.createSettingsCodec()).apply((Applicative)instance, RotatedInfestedBlock::new));

    public MapCodec<RotatedInfestedBlock> getCodec() {
        return CODEC;
    }

    public RotatedInfestedBlock(Block block, AbstractBlock.Settings settings) {
        super(block, settings);
        this.setDefaultState((BlockState)this.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return PillarBlock.changeRotation(state, rotation);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PillarBlock.AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(PillarBlock.AXIS, ctx.getSide().getAxis());
    }
}

