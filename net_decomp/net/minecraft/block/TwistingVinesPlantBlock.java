/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractPlantBlock;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class TwistingVinesPlantBlock
extends AbstractPlantBlock {
    public static final MapCodec<TwistingVinesPlantBlock> CODEC = TwistingVinesPlantBlock.createCodec(TwistingVinesPlantBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(8.0, 0.0, 16.0);

    public MapCodec<TwistingVinesPlantBlock> getCodec() {
        return CODEC;
    }

    public TwistingVinesPlantBlock(AbstractBlock.Settings settings) {
        super(settings, Direction.UP, SHAPE, false);
    }

    @Override
    protected AbstractPlantStemBlock getStem() {
        return (AbstractPlantStemBlock)Blocks.TWISTING_VINES;
    }
}

