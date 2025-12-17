/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.BlockPos;

public interface BlockEntityTickInvoker {
    public void tick();

    public boolean isRemoved();

    public BlockPos getPos();

    public String getName();
}

