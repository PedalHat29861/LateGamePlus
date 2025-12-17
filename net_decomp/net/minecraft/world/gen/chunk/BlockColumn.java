/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;

public interface BlockColumn {
    public BlockState getState(int var1);

    public void setState(int var1, BlockState var2);
}

