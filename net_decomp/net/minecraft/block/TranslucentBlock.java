/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class TranslucentBlock
extends Block {
    public static final MapCodec<TranslucentBlock> CODEC = TranslucentBlock.createCodec(TranslucentBlock::new);

    protected MapCodec<? extends TranslucentBlock> getCodec() {
        return CODEC;
    }

    protected TranslucentBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            return true;
        }
        return super.isSideInvisible(state, stateFrom, direction);
    }
}

