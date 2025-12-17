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

public class PlayerSkullBlock
extends SkullBlock {
    public static final MapCodec<PlayerSkullBlock> CODEC = PlayerSkullBlock.createCodec(PlayerSkullBlock::new);

    public MapCodec<PlayerSkullBlock> getCodec() {
        return CODEC;
    }

    protected PlayerSkullBlock(AbstractBlock.Settings settings) {
        super(SkullBlock.Type.PLAYER, settings);
    }
}

