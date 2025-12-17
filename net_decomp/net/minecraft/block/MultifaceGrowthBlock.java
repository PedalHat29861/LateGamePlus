/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.MultifaceGrower;

public abstract class MultifaceGrowthBlock
extends MultifaceBlock {
    public MultifaceGrowthBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public abstract MapCodec<? extends MultifaceGrowthBlock> getCodec();

    public abstract MultifaceGrower getGrower();
}

