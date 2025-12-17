/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;

public class WallPlayerSkullBlock
extends WallSkullBlock {
    public static final MapCodec<WallPlayerSkullBlock> CODEC = WallPlayerSkullBlock.createCodec(WallPlayerSkullBlock::new);

    public MapCodec<WallPlayerSkullBlock> getCodec() {
        return CODEC;
    }

    protected WallPlayerSkullBlock(AbstractBlock.Settings settings) {
        super(SkullBlock.Type.PLAYER, settings);
    }
}

