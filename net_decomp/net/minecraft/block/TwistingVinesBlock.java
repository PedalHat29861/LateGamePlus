/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineLogic;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesBlock
extends AbstractPlantStemBlock {
    public static final MapCodec<TwistingVinesBlock> CODEC = TwistingVinesBlock.createCodec(TwistingVinesBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(8.0, 0.0, 15.0);

    public MapCodec<TwistingVinesBlock> getCodec() {
        return CODEC;
    }

    public TwistingVinesBlock(AbstractBlock.Settings settings) {
        super(settings, Direction.UP, SHAPE, false, 0.1);
    }

    @Override
    protected int getGrowthLength(Random random) {
        return VineLogic.getGrowthLength(random);
    }

    @Override
    protected Block getPlant() {
        return Blocks.TWISTING_VINES_PLANT;
    }

    @Override
    protected boolean chooseStemState(BlockState state) {
        return VineLogic.isValidForWeepingStem(state);
    }
}

