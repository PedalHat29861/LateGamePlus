/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public interface InventoryProvider {
    public SidedInventory getInventory(BlockState var1, WorldAccess var2, BlockPos var3);
}

